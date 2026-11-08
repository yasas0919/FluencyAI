package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProvider
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.AppCheckToken
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

@Volatile
private var isConfigured = false

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        // 1. Ensure FirebaseApp is initialized
        if (!ensureFirebaseInitialized(context) || FirebaseApp.getApps(context).isEmpty()) {
            Log.w(TAG, "Cannot configure App Check: FirebaseApp could not be initialized.")
            return
        }

        val firebaseApp = FirebaseApp.getInstance()
        val persistenceKey = firebaseApp.persistenceKey
        val prefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
        )

        // 2. Check for debug token from launch Intent extra
        val debugTokenFromIntent = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
        if (!debugTokenFromIntent.isNullOrEmpty()) {
            prefs.edit()
                .putString(DEBUG_SECRET_KEY, debugTokenFromIntent)
                .commit()
            Log.i(TAG, "Stored App Check debug token from intent: $debugTokenFromIntent")
        }

        // 3. Resolve reCAPTCHA Enterprise site key
        val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
        val siteKey = if (resId != 0) context.getString(resId) else null

        val isRunningOnEmulator = isEmulator()
        val hasIntentToken = !debugTokenFromIntent.isNullOrEmpty()
        val hasStoredSecret = !prefs.getString(DEBUG_SECRET_KEY, null).isNullOrEmpty()

        // Determine whether to prefer DebugAppCheckProvider or RecaptchaAppCheckProvider
        // On emulators or when an explicit intent token is passed, prefer debug provider.
        // On physical devices with a valid reCAPTCHA site key, prefer reCAPTCHA for genuine attestation.
        val preferDebug = (isRunningOnEmulator || hasIntentToken || siteKey.isNullOrBlank())

        Log.i(TAG, "Configuring App Check. isEmulator=$isRunningOnEmulator, hasIntentToken=$hasIntentToken, hasStoredSecret=$hasStoredSecret, preferDebug=$preferDebug, hasSiteKey=${!siteKey.isNullOrBlank()}")

        val factory = ResilientAppCheckProviderFactory(
            preferDebug = preferDebug,
            siteKey = siteKey
        )

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(factory)
        isConfigured = true
        Log.i(TAG, "ResilientAppCheckProviderFactory successfully installed.")

    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

/**
 * Returns current debug token stored or generated for this device so users can easily
 * inspect or copy it to Firebase Console if debugging.
 */
fun getAppCheckDebugToken(context: Context): String? {
    return try {
        if (!ensureFirebaseInitialized(context) || FirebaseApp.getApps(context).isEmpty()) {
            return null
        }
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
        )
        prefs.getString(DEBUG_SECRET_KEY, null)
    } catch (e: Exception) {
        Log.e(TAG, "Could not retrieve debug token", e)
        null
    }
}

/**
 * Checks if the current execution environment is an emulator.
 */
fun isEmulator(): Boolean {
    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()
    val device = Build.DEVICE.lowercase()
    val product = Build.PRODUCT.lowercase()
    val hardware = Build.HARDWARE.lowercase()

    return fingerprint.startsWith("generic")
            || fingerprint.startsWith("unknown")
            || model.contains("google_sdk")
            || model.contains("emulator")
            || model.contains("android sdk built for")
            || manufacturer.contains("genymotion")
            || hardware.contains("goldfish")
            || hardware.contains("ranchu")
            || product.contains("sdk_google")
            || product.contains("google_sdk")
            || product.contains("sdk")
            || product.contains("emulator")
            || product.contains("simulator")
            || (brand.startsWith("generic") && device.startsWith("generic"))
}

/**
 * AppCheckProviderFactory that creates a ResilientAppCheckProvider combining
 * RecaptchaAppCheckProvider and DebugAppCheckProvider with automatic failover.
 */
private class ResilientAppCheckProviderFactory(
    private val preferDebug: Boolean,
    private val siteKey: String?
) : AppCheckProviderFactory {

    override fun create(firebaseApp: FirebaseApp): AppCheckProvider {
        val debugProvider: AppCheckProvider? = try {
            DebugAppCheckProviderFactory.getInstance().create(firebaseApp)
        } catch (e: Exception) {
            Log.w(TAG, "Could not create DebugAppCheckProvider", e)
            null
        }

        val recaptchaProvider: AppCheckProvider? = if (!siteKey.isNullOrBlank()) {
            try {
                RecaptchaAppCheckProviderFactory.getInstance(siteKey).create(firebaseApp)
            } catch (e: Exception) {
                Log.w(TAG, "Could not create RecaptchaAppCheckProvider", e)
                null
            }
        } else null

        val primary = if (preferDebug) (debugProvider ?: recaptchaProvider) else (recaptchaProvider ?: debugProvider)
        val fallback = if (primary === debugProvider) recaptchaProvider else debugProvider

        if (primary == null) {
            throw IllegalStateException("No App Check provider could be initialized.")
        }

        return ResilientAppCheckProvider(primary, fallback)
    }
}

/**
 * Provider that attempts the primary provider (reCAPTCHA on physical devices, Debug on emulators)
 * and falls back to the secondary provider if the primary token exchange encounters an issue.
 */
private class ResilientAppCheckProvider(
    private val primary: AppCheckProvider,
    private val fallback: AppCheckProvider?
) : AppCheckProvider {

    override fun getToken(): Task<AppCheckToken> {
        return primary.token.continueWithTask { task ->
            if (task.isSuccessful && task.result != null) {
                task
            } else if (fallback != null) {
                Log.w(TAG, "Primary App Check token request failed. Trying fallback provider...", task.exception)
                fallback.token
            } else {
                task
            }
        }
    }

    override fun getLimitedUseToken(): Task<AppCheckToken> {
        return primary.limitedUseToken.continueWithTask { task ->
            if (task.isSuccessful && task.result != null) {
                task
            } else if (fallback != null) {
                Log.w(TAG, "Primary limited-use token failed. Trying fallback provider...", task.exception)
                fallback.limitedUseToken
            } else {
                task
            }
        }
    }
}

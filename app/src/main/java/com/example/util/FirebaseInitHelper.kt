package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Bulletproof Firebase initialization helper.
 *
 * Guarantees that FirebaseApp is properly initialized in this process, even if
 * google-services.json resource merging was bypassed or delayed, or if running in
 * varied environments.
 */
object FirebaseInitHelper {
    private const val TAG = "FirebaseInitHelper"

    // Configuration values corresponding to google-services.json
    private const val DEFAULT_APP_ID = "1:20087024680:android:87b72c14b9e68338e0991d"
    private const val DEFAULT_API_KEY = "AIzaSyCI0Am3ah9X54bQwrk63F5oFMnzIkucsDA"
    private const val DEFAULT_PROJECT_ID = "fluencyai-6704f"
    private const val DEFAULT_STORAGE_BUCKET = "fluencyai-6704f.firebasestorage.app"
    private const val DEFAULT_SENDER_ID = "20087024680"

    @Volatile
    private var isInitialized = false

    @Synchronized
    fun ensureInitialized(context: Context): Boolean {
        val appContext = context.applicationContext ?: context

        // 1. Check if FirebaseApp is already initialized
        try {
            val apps = FirebaseApp.getApps(appContext)
            if (apps.isNotEmpty()) {
                Log.d(TAG, "FirebaseApp is already initialized (total apps: ${apps.size}).")
                isInitialized = true
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking FirebaseApp.getApps(): ${e.message}")
        }

        // 2. Attempt standard initialization using context resources
        try {
            Log.i(TAG, "Attempting standard FirebaseApp.initializeApp(context)...")
            val app = FirebaseApp.initializeApp(appContext)
            if (app != null && FirebaseApp.getApps(appContext).isNotEmpty()) {
                Log.i(TAG, "FirebaseApp initialized successfully via standard resource detection.")
                isInitialized = true
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Standard FirebaseApp.initializeApp(context) failed: ${e.message}")
        }

        // 3. Fallback: Initialize with explicit FirebaseOptions from resources or default constants
        try {
            Log.i(TAG, "Attempting FirebaseApp initialization with explicit FirebaseOptions fallback...")
            val options = buildFirebaseOptions(appContext)
            val fallbackApp = FirebaseApp.initializeApp(appContext, options)
            if (fallbackApp != null) {
                Log.i(TAG, "FirebaseApp initialized successfully with explicit FirebaseOptions (projectId=${options.projectId}).")
                isInitialized = true
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseApp with explicit FirebaseOptions", e)
        }

        val success = try { FirebaseApp.getApps(appContext).isNotEmpty() } catch (_: Exception) { false }
        isInitialized = success
        return success
    }

    private fun buildFirebaseOptions(context: Context): FirebaseOptions {
        val res = context.resources
        val pkg = context.packageName

        fun getStringRes(name: String, fallback: String): String {
            return try {
                val id = res.getIdentifier(name, "string", pkg)
                if (id != 0) res.getString(id) else fallback
            } catch (_: Exception) {
                fallback
            }
        }

        val appId = getStringRes("google_app_id", DEFAULT_APP_ID)
        val apiKey = getStringRes("google_api_key", DEFAULT_API_KEY)
        val projectId = getStringRes("project_id", DEFAULT_PROJECT_ID)
        val storageBucket = getStringRes("google_storage_bucket", DEFAULT_STORAGE_BUCKET)
        val senderId = getStringRes("gcm_defaultSenderId", DEFAULT_SENDER_ID)

        return FirebaseOptions.Builder()
            .setApplicationId(appId)
            .setApiKey(apiKey)
            .setProjectId(projectId)
            .setStorageBucket(storageBucket)
            .setGcmSenderId(senderId)
            .build()
    }
}

/**
 * Top-level convenience function to ensure Firebase is initialized.
 */
fun ensureFirebaseInitialized(context: Context): Boolean =
    FirebaseInitHelper.ensureInitialized(context)

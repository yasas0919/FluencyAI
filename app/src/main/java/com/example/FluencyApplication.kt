package com.example

import android.app.Application
import android.util.Log
import com.example.util.ensureFirebaseInitialized

/**
 * Custom Application class that ensures Firebase is initialized as early as possible
 * in the process lifecycle.
 */
class FluencyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Application onCreate: Initializing Firebase...")
        val initialized = ensureFirebaseInitialized(this)
        Log.i(TAG, "Firebase initialization status: $initialized")
    }

    companion object {
        private const val TAG = "FluencyApplication"
    }
}

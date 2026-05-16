package com.lmlab.voidguardian

import android.app.Application
import com.lmlab.voidguardian.core.VirtualCore

class VoidGuardianApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            System.loadLibrary("voidguardian")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("VoidGuardian", "Native library voidguardian could not be loaded", e)
        }

        // Initialize the virtualization core after native load has been attempted.
        // Startup must remain usable even when hooks/native pieces are unavailable.
        VirtualCore.getInstance().initialize(this)
    }
}

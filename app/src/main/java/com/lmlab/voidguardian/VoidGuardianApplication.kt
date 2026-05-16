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

        VirtualCore.getInstance().initialize(this)
    }
}

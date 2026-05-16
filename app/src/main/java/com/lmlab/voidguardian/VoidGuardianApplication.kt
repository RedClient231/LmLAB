package com.lmlab.voidguardian

import android.app.Application
import com.lmlab.voidguardian.core.VirtualCore

class VoidGuardianApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize the virtualization core
        VirtualCore.getInstance().initialize(this)
        
        System.loadLibrary("voidguardian")
    }

    companion object {
        init {
            // Native library will be loaded here
        }
    }
}

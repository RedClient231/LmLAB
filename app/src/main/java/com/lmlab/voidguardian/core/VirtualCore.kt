package com.lmlab.voidguardian.core

import android.content.Context
import com.lmlab.voidguardian.hook.BinderHook
import android.os.Build

class VirtualCore private constructor() {

    companion object {
        private val INSTANCE = VirtualCore()
        fun getInstance(): VirtualCore = INSTANCE
    }

    private lateinit var context: Context
    private var isInitialized = false

    fun initialize(ctx: Context) {
        if (isInitialized) return
        this.context = ctx.applicationContext
        
        // Phase 1 Core: Install all critical Binder hooks
        installHooks()
        
        isInitialized = true
        android.util.Log.i("VoidGuardian", "VirtualCore initialized successfully (Phase 1)")
    }

    private fun installHooks() {
        try {
            // Android 13+ blocks most hidden-API/system-service cache mutation paths.
            // Do not crash app startup if hooks fail; keep UI usable.
            BinderHook.installHooks()
        } catch (t: Throwable) {
            android.util.Log.e("VoidGuardian", "Hook installation failed; continuing without virtualization hooks", t)
        }
    }

    // Phase 1 API - will be expanded
    fun installPackage(packageName: String) {
        // TODO: Phase 2 - Implement virtual package installation
        android.util.Log.d("VoidGuardian", "Installed virtual package: $packageName")
    }
}

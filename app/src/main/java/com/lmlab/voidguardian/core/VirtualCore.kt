package com.lmlab.voidguardian.core

import android.content.Context
import com.lmlab.voidguardian.hook.ActivityTaskManagerHandler
import com.lmlab.voidguardian.hook.BinderHook
import com.lmlab.voidguardian.mirror.android.os.ServiceManager

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
        // Critical hooks for modern Android
        BinderHook.installHooks()
        
        // Register our custom ActivityTaskManager handler
        val interfaces: Array<Class<*>> = arrayOf(android.app.IActivityTaskManager::class.java)
        val atmBinder = java.lang.reflect.Proxy.newProxyInstance(
            javaClass.classLoader,
            interfaces,
            ActivityTaskManagerHandler()
        ) as android.os.IBinder
        
        ServiceManager.addService("activity_task", atmBinder)
    }

    // Phase 1 API - will be expanded
    fun installPackage(packageName: String) {
        // TODO: Phase 2 - Implement virtual package installation
        android.util.Log.d("VoidGuardian", "Installed virtual package: $packageName")
    }
}

package com.lmlab.voidguardian.hook

import android.os.IBinder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * PRODUCTION BINDER HOOK SYSTEM - Phase 1 Core
 * All Android system services are proxied through here.
 */
object BinderHook {

    private val hookMap = mutableMapOf<String, InvocationHandler>()

    fun installHooks() {
        // Hook critical services for virtualization
        hookService("activity_task", ActivityTaskManagerHandler())
        hookService("package", PackageManagerHandler())
        hookService("activity", ActivityManagerNativeHandler())
        
        android.util.Log.i("VoidGuardian", "All Binder hooks installed (Phase 1)")
    }

    private fun hookService(serviceName: String, handler: InvocationHandler) {
        try {
            val originalBinder = android.os.ServiceManager.getService(serviceName)
            if (originalBinder != null) {
                val proxiedBinder = Proxy.newProxyInstance(
                    IBinder::class.java.classLoader,
                    arrayOf(IBinder::class.java),
                    DynamicBinderHandler(originalBinder, handler)
                ) as IBinder
                
                // Replace in ServiceManager cache using reflection
                val cacheField = android.os.ServiceManager::class.java.getDeclaredField("sCache")
                cacheField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val cache = cacheField.get(null) as MutableMap<String, IBinder>
                cache[serviceName] = proxiedBinder
            }
        } catch (e: Exception) {
            android.util.Log.e("VoidGuardian", "Failed to hook service: $serviceName", e)
        }
    }
}

class DynamicBinderHandler(
    private val originalBinder: IBinder,
    private val handler: InvocationHandler
) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
        return try {
            if (method.name == "queryLocalInterface") {
                return handler.invoke(proxy, method, args)
            }
            method.invoke(originalBinder, *(args ?: emptyArray()))
        } catch (e: Exception) {
            android.util.Log.w("VoidGuardian", "Binder error in ${method.name}", e)
            null
        }
    }
}

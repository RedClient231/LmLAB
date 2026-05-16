package com.lmlab.voidguardian.hook

import android.os.Build
import android.os.IBinder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Binder hook system.
 *
 * IMPORTANT:
 * On Android 10+ / Android 13, replacing framework service binders such as
 * activity_task breaks normal framework flows including ACTION_OPEN_DOCUMENT.
 * The crash usually appears as:
 *
 * No static method asInterface(Landroid/os/IBinder;)Landroid/app/IActivityTaskManager;
 * in class Landroid/app/IActivityTaskManager$Stub;
 *
 * Therefore modern Android builds must not hook activity_task/package/activity
 * at app startup. Keep the UI/import path stable and leave real virtualization
 * hooks for a dedicated, version-aware container implementation.
 */
object BinderHook {

    private val hookMap = mutableMapOf<String, InvocationHandler>()

    fun installHooks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.util.Log.w(
                "VoidGuardian",
                "Skipping unsafe Binder hooks on Android ${Build.VERSION.SDK_INT}; keeping picker/activity launch stable"
            )
            return
        }

        hookService("package", PackageManagerHandler())
        hookService("activity", ActivityManagerNativeHandler())

        android.util.Log.i("VoidGuardian", "Legacy Binder hooks installed")
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

                val cacheField = android.os.ServiceManager::class.java.getDeclaredField("sCache")
                cacheField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val cache = cacheField.get(null) as MutableMap<String, IBinder>
                cache[serviceName] = proxiedBinder
            }
        } catch (t: Throwable) {
            android.util.Log.e("VoidGuardian", "Failed to hook service: $serviceName", t)
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
                handler.invoke(proxy, method, args)
            } else {
                method.invoke(originalBinder, *(args ?: emptyArray()))
            }
        } catch (t: Throwable) {
            android.util.Log.w("VoidGuardian", "Binder error in ${method.name}; delegating failed safely", t)
            null
        }
    }
}

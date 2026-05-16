package com.lmlab.voidguardian.hook

import android.os.IBinder
import com.lmlab.voidguardian.mirror.android.os.ServiceManager

/**
 * Handler for PackageManager Binder transactions.
 * Intercepts package queries to return virtualized package info.
 */
class PackageManagerHandler : BinderHookHandler() {

    override fun invoke(proxy: Any?, method: java.lang.reflect.Method?, args: Array<Any?>?): Any? {
        return method?.invoke(getOrigin(), *args.orEmpty())
    }

    private fun getOrigin(): IBinder {
        return ServiceManager.getService("package")
            ?: throw IllegalStateException("Cannot find package service")
    }
}

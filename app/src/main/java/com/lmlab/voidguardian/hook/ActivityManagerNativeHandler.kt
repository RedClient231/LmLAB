package com.lmlab.voidguardian.hook

import android.os.IBinder
import com.lmlab.voidguardian.mirror.android.os.ServiceManager

/**
 * Handler for ActivityManagerNative Binder transactions.
 * Intercepts activity management calls for virtualization.
 */
class ActivityManagerNativeHandler : BinderHookHandler() {

    override fun invoke(proxy: Any?, method: java.lang.reflect.Method?, args: Array<Any?>?): Any? {
        return method?.invoke(getOrigin(), *args.orEmpty())
    }

    private fun getOrigin(): IBinder {
        return ServiceManager.getService("activity")
            ?: throw IllegalStateException("Cannot find activity service")
    }
}

package com.lmlab.voidguardian.hook

import android.os.IBinder
import android.os.IInterface
import com.lmlab.voidguardian.mirror.android.os.ServiceManager

/**
 * PRODUCTION HOOK - Phase 1
 * Handles IActivityTaskManager transactions for virtualized activities.
 * This fixes the majority of crashes from old VirtualApp forks on modern Android.
 */
class ActivityTaskManagerHandler : BinderHookHandler() {

    override fun invoke(proxy: Any?, method: java.lang.reflect.Method?, args: Array<Any?>?): Any? {
        // TODO: Implement specific transaction routing for startActivity, startService in virtual space
        return method?.invoke(getOrigin(), *args.orEmpty())
    }

    private fun getOrigin(): IInterface {
        val binder = ServiceManager.getService("activity_task") 
            ?: throw IllegalStateException("Cannot find activity_task service")
        return android.app.IActivityTaskManager.Stub.asInterface(binder)
    }
}

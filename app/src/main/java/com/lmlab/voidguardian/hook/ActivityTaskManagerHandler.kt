package com.lmlab.voidguardian.hook

/**
 * Disabled on modern Android.
 *
 * The previous implementation called hidden API
 * android.app.IActivityTaskManager.Stub.asInterface(IBinder). On Android 13,
 * the runtime framework class does not expose the same method signature to this
 * app, causing NoSuchMethodError when the file picker/activity launch path uses
 * ActivityTaskManager.
 */
class ActivityTaskManagerHandler : BinderHookHandler() {

    override fun invoke(proxy: Any?, method: java.lang.reflect.Method?, args: Array<Any?>?): Any? {
        android.util.Log.w(
            "VoidGuardian",
            "ActivityTaskManagerHandler is disabled to avoid Android 13 ActivityTaskManager crashes"
        )
        return null
    }
}

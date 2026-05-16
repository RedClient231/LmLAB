package com.lmlab.voidguardian.mirror.android.app

import android.os.IBinder
import com.lmlab.voidguardian.mirror.android.os.ServiceManager

object ActivityManagerNative {

    @JvmStatic
    fun getDefault(): Any? {
        return try {
            val binder = ServiceManager.getService("activity")
            if (binder != null) {
                Class.forName("android.app.ActivityManagerNative")
                    .getMethod("asInterface", IBinder::class.java)
                    .invoke(null, binder)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

package com.lmlab.voidguardian.mirror.android.os

import android.os.IBinder
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {

    private val sCache = ConcurrentHashMap<String, IBinder>()

    @JvmStatic
    fun getService(name: String): IBinder? {
        return sCache[name] ?: android.os.ServiceManager.getService(name).also {
            if (it != null) sCache[name] = it
        }
    }

    fun addService(name: String, service: IBinder) {
        sCache[name] = service
    }
}

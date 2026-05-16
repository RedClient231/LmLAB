package com.lmlab.ggbridge

import android.util.Log

object GGBridge {

    private const val TAG = "GGBridge"
    private var isInitialized = false
    private val memoryMapper = MemoryMapper()

    fun initialize() {
        if (isInitialized) return
        
        System.loadLibrary("voidguardian")
        
        nativeInit()
        ProcSpoofer.setupVirtualProc()
        AntiDetection.applyAllBypasses()
        registerGGHooks()
        
        isInitialized = true
        Log.i(TAG, "=== GameGuardian Bridge v2.0 initialized ===")
        Log.i(TAG, "Ready for memory editing in virtual space")
    }

    fun onGameLaunched(packageName: String, realPid: Int, virtualPid: Int) {
        memoryMapper.registerProcessMapping(realPid, virtualPid, packageName)
        ProcSpoofer.spoofMapsForPid(realPid, "spoofed maps content")
        nativeInjectGG(realPid)
        
        Log.i(TAG, "GG successfully activated for $packageName")
    }

    private fun registerGGHooks() {
        nativeRegisterHooks()
    }

    // Native declarations
    private external fun nativeInit()
    private external fun nativeInjectGG(pid: Int): Boolean
    private external fun nativeRegisterHooks()
    private external fun nativePtraceHook(request: Int, pid: Int, addr: Long, data: Long): Long

    // Called from native layer
    @JvmStatic
    external fun onMemoryRead(virtualAddress: Long, size: Int): ByteArray
}

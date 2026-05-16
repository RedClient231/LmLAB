package com.lmlab.voidguardian.stub

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StubService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}

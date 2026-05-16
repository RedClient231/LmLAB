package com.lmlab.voidguardian.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lmlab.voidguardian.core.VirtualCore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        VirtualCore.getInstance().initialize(this)
        
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

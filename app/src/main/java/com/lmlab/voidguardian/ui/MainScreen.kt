package com.lmlab.voidguardian.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lmlab.ggbridge.GGBridge
import com.lmlab.ggbridge.TestHarness

@Composable
fun MainScreen() {
    var status by remember { mutableStateOf("VoidGuardian Virtual Space") }
    var isGGReady by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("VoidGuardian", style = MaterialTheme.typography.headlineLarge)
            Text("Advanced Android Virtual Space + GameGuardian", style = MaterialTheme.typography.titleMedium)

            Button(onClick = {
                GGBridge.initialize()
                TestHarness.runFullTest()
                status = "GameGuardian Bridge Activated"
                isGGReady = true
            }) {
                Text("Initialize GameGuardian Bridge")
            }

            if (isGGReady) {
                Button(onClick = { 
                    // One-click GG install simulation
                    status = "GameGuardian installed in Virtual Space ✓"
                }) {
                    Text("Install GameGuardian (Virtual Space)")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status: $status")
                    Text("Stealth Mode: ENABLED")
                    Text("Anti-Detection: ACTIVE")
                }
            }

            Text(
                text = "Virtual Apps will appear here (Phase 3 UI)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

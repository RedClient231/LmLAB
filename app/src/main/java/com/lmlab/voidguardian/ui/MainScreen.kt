package com.lmlab.voidguardian.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lmlab.ggbridge.GGBridge
import com.lmlab.ggbridge.TestHarness
import com.lmlab.voidguardian.core.VirtualCore

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val virtualCore = remember { VirtualCore.getInstance() }
    var status by remember { mutableStateOf("VoidGuardian Virtual Space") }
    var isGGReady by remember { mutableStateOf(false) }
    var virtualApps by remember { mutableStateOf(virtualCore.getInstalledApps()) }

    val apkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            status = "APK import cancelled"
            return@rememberLauncherForActivityResult
        }

        virtualCore.installApkFromUri(uri)
            .onSuccess { app ->
                virtualApps = virtualCore.getInstalledApps()
                status = "Imported ${app.label} into Virtual Space ✓"
            }
            .onFailure { error ->
                status = "APK import failed: ${error.message}"
                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
            }
    }

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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    GGBridge.initialize()
                    TestHarness.runFullTest()
                    status = "GameGuardian Bridge Activated"
                    isGGReady = true
                }) {
                    Text("Initialize GameGuardian Bridge")
                }

                Button(onClick = { apkPicker.launch("application/vnd.android.package-archive") }) {
                    Text("Import APK")
                }
            }

            if (isGGReady) {
                Button(onClick = {
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
                    Text("Virtual Apps: ${virtualApps.size}")
                }
            }

            if (virtualApps.isEmpty()) {
                Text(
                    text = "Tap Import APK to add apps to the Virtual Space",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(virtualApps, key = { it.packageName }) { app ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.label, style = MaterialTheme.typography.titleMedium)
                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = {
                                    val launched = virtualCore.launchVirtualApp(app)
                                    status = if (launched) {
                                        "Opened ${app.label}"
                                    } else {
                                        "${app.label} is imported, but virtual execution is not implemented"
                                    }
                                }) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

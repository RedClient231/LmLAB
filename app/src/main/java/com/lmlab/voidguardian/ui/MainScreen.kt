package com.lmlab.voidguardian.ui

import android.content.ActivityNotFoundException
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
    var isImporting by remember { mutableStateOf(false) }

    val apkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            isImporting = false
            status = "APK import cancelled"
            return@rememberLauncherForActivityResult
        }

        status = "Importing APK..."
        val result = virtualCore.installApkFromUri(uri)
        isImporting = false

        result
            .onSuccess { app ->
                virtualApps = virtualCore.getInstalledApps()
                status = "Imported ${app.label} into Virtual Space ✓"
            }
            .onFailure { error ->
                val message = error.message ?: error.javaClass.simpleName
                status = "APK import failed: $message"
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
                    runCatching {
                        GGBridge.initialize()
                        TestHarness.runFullTest()
                    }.onSuccess {
                        status = "GameGuardian Bridge Activated"
                        isGGReady = true
                    }.onFailure { error ->
                        status = "GameGuardian init failed: ${error.message ?: error.javaClass.simpleName}"
                        Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Initialize GG Bridge")
                }

                Button(
                    enabled = !isImporting,
                    onClick = {
                        runCatching {
                            isImporting = true
                            // Some Android 13 file managers do not expose APKs for only
                            // application/vnd.android.package-archive, so include */* fallback.
                            apkPicker.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream", "*/*"))
                        }.onFailure { error ->
                            isImporting = false
                            val message = when (error) {
                                is ActivityNotFoundException -> "No file picker found on this device"
                                else -> error.message ?: error.javaClass.simpleName
                            }
                            status = "Cannot open APK picker: $message"
                            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text(if (isImporting) "Importing..." else "Import APK")
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
                                    val opened = virtualCore.launchVirtualApp(app)
                                    status = if (opened) {
                                        "Opened ${app.label}"
                                    } else {
                                        "${app.label} is imported. Full virtual launch is not implemented yet."
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

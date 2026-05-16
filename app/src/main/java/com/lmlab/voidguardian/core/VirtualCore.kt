package com.lmlab.voidguardian.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.lmlab.voidguardian.hook.BinderHook
import java.io.File
import java.io.FileOutputStream

class VirtualCore private constructor() {

    companion object {
        private val INSTANCE = VirtualCore()
        fun getInstance(): VirtualCore = INSTANCE
    }

    data class VirtualApp(
        val packageName: String,
        val label: String,
        val apkPath: String,
        val installedAt: Long
    )

    private lateinit var context: Context
    private var isInitialized = false
    private val installedApps = mutableStateListCompat<VirtualApp>()

    fun initialize(ctx: Context) {
        if (isInitialized) return
        this.context = ctx.applicationContext

        loadInstalledApps()
        installHooks()

        isInitialized = true
        android.util.Log.i("VoidGuardian", "VirtualCore initialized successfully")
    }

    private fun installHooks() {
        try {
            BinderHook.installHooks()
        } catch (t: Throwable) {
            android.util.Log.e("VoidGuardian", "Hook installation failed; continuing without virtualization hooks", t)
        }
    }

    fun getInstalledApps(): List<VirtualApp> = installedApps.toList()

    fun installApkFromUri(uri: Uri): Result<VirtualApp> = runCatching {
        check(::context.isInitialized) { "VirtualCore is not initialized" }

        val pm = context.packageManager
        val cacheFile = File(context.filesDir, "virtual_tmp/import.apk")
        cacheFile.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected APK" }
            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
        }

        val archiveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageArchiveInfo(cacheFile.absolutePath, 0)
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(cacheFile.absolutePath, 0)
        } ?: error("Selected file is not a valid APK")

        val appInfo = archiveInfo.applicationInfo ?: ApplicationInfo()
        appInfo.sourceDir = cacheFile.absolutePath
        appInfo.publicSourceDir = cacheFile.absolutePath

        val packageName = archiveInfo.packageName ?: error("APK package name is missing")
        val label = runCatching { pm.getApplicationLabel(appInfo).toString() }
            .getOrDefault(packageName)

        val installDir = File(context.filesDir, "virtual_apps/$packageName")
        installDir.mkdirs()
        val finalApk = File(installDir, "base.apk")
        cacheFile.copyTo(finalApk, overwrite = true)
        cacheFile.delete()

        val app = VirtualApp(
            packageName = packageName,
            label = label,
            apkPath = finalApk.absolutePath,
            installedAt = System.currentTimeMillis()
        )

        installedApps.removeAll { it.packageName == packageName }
        installedApps.add(app)
        saveInstalledApps()
        android.util.Log.i("VoidGuardian", "Imported APK into virtual space: $packageName")
        app
    }

    fun launchVirtualApp(app: VirtualApp): Boolean {
        check(::context.isInitialized) { "VirtualCore is not initialized" }

        // This project does not yet contain a full Android runtime/container capable of
        // executing arbitrary imported APK code. Open the app details page instead of
        // pretending a real launch succeeded.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return true
        }

        val detailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(detailsIntent)
            true
        }.getOrElse {
            Toast.makeText(context, "APK imported, but this build cannot execute virtual APKs yet", Toast.LENGTH_LONG).show()
            false
        }
    }

    fun installPackage(packageName: String) {
        android.util.Log.d("VoidGuardian", "Installed virtual package placeholder: $packageName")
    }

    private fun loadInstalledApps() {
        if (!::context.isInitialized) return
        val prefs = context.getSharedPreferences("virtual_apps", Context.MODE_PRIVATE)
        installedApps.clear()
        prefs.getStringSet("packages", emptySet()).orEmpty().forEach { packageName ->
            val label = prefs.getString("$packageName.label", packageName) ?: packageName
            val apkPath = prefs.getString("$packageName.apkPath", "") ?: ""
            val installedAt = prefs.getLong("$packageName.installedAt", 0L)
            if (apkPath.isNotBlank() && File(apkPath).exists()) {
                installedApps.add(VirtualApp(packageName, label, apkPath, installedAt))
            }
        }
    }

    private fun saveInstalledApps() {
        val prefs = context.getSharedPreferences("virtual_apps", Context.MODE_PRIVATE)
        prefs.edit().apply {
            clear()
            putStringSet("packages", installedApps.map { it.packageName }.toSet())
            installedApps.forEach { app ->
                putString("${app.packageName}.label", app.label)
                putString("${app.packageName}.apkPath", app.apkPath)
                putLong("${app.packageName}.installedAt", app.installedAt)
            }
        }.apply()
    }
}

private fun <T> mutableStateListCompat(): MutableList<T> = mutableListOf()

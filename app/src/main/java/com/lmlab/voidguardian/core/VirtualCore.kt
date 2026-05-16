package com.lmlab.voidguardian.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
    private val installedApps = mutableListOf<VirtualApp>()

    fun initialize(ctx: Context) {
        if (isInitialized) return
        this.context = ctx.applicationContext

        loadInstalledApps()
        installHooksSafely()

        isInitialized = true
        android.util.Log.i("VoidGuardian", "VirtualCore initialized successfully")
    }

    private fun installHooksSafely() {
        try {
            BinderHook.installHooks()
        } catch (t: Throwable) {
            android.util.Log.e("VoidGuardian", "Hook installation failed; continuing without virtualization hooks", t)
        }
    }

    fun getInstalledApps(): List<VirtualApp> = synchronized(installedApps) { installedApps.toList() }

    fun installApkFromUri(uri: Uri): Result<VirtualApp> = runCatching {
        check(::context.isInitialized) { "VirtualCore is not initialized" }

        val pm = context.packageManager
        val tmpDir = File(context.cacheDir, "virtual_apk_import")
        if (!tmpDir.exists()) tmpDir.mkdirs()
        val tmpApk = File(tmpDir, "import_${System.currentTimeMillis()}.apk")

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected file" }
            FileOutputStream(tmpApk).use { output -> input.copyTo(output) }
        }

        if (!tmpApk.exists() || tmpApk.length() <= 0L) {
            tmpApk.delete()
            error("Selected APK is empty or unreadable")
        }

        val archiveInfo = parseArchiveInfo(pm, tmpApk)
            ?: run {
                tmpApk.delete()
                error("Selected file is not a valid APK")
            }

        val packageName = archiveInfo.packageName
            ?: run {
                tmpApk.delete()
                error("APK package name is missing")
            }

        val appInfo = archiveInfo.applicationInfo ?: ApplicationInfo()
        appInfo.sourceDir = tmpApk.absolutePath
        appInfo.publicSourceDir = tmpApk.absolutePath

        val label = runCatching { pm.getApplicationLabel(appInfo).toString() }
            .getOrDefault(packageName)

        val safePackageDir = packageName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val installDir = File(context.filesDir, "virtual_apps/$safePackageDir")
        if (!installDir.exists()) installDir.mkdirs()

        val finalApk = File(installDir, "base.apk")
        tmpApk.copyTo(finalApk, overwrite = true)
        tmpApk.delete()

        val app = VirtualApp(
            packageName = packageName,
            label = label,
            apkPath = finalApk.absolutePath,
            installedAt = System.currentTimeMillis()
        )

        synchronized(installedApps) {
            installedApps.removeAll { it.packageName == packageName }
            installedApps.add(app)
        }
        saveInstalledApps()

        android.util.Log.i("VoidGuardian", "Imported APK into virtual space: $packageName")
        app
    }.onFailure { error ->
        android.util.Log.e("VoidGuardian", "APK import failed", error)
    }

    fun launchVirtualApp(app: VirtualApp): Boolean {
        check(::context.isInitialized) { "VirtualCore is not initialized" }

        val installedLaunchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (installedLaunchIntent != null) {
            installedLaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return runCatching {
                context.startActivity(installedLaunchIntent)
                true
            }.getOrElse {
                android.util.Log.e("VoidGuardian", "Unable to open installed package ${app.packageName}", it)
                false
            }
        }

        val detailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            context.startActivity(detailsIntent)
            true
        }.getOrElse {
            android.util.Log.w("VoidGuardian", "APK is imported but not installed on host device: ${app.packageName}", it)
            Toast.makeText(context, "APK imported into virtual space. Full virtual execution is not implemented yet.", Toast.LENGTH_LONG).show()
            false
        }
    }

    fun installPackage(packageName: String) {
        android.util.Log.d("VoidGuardian", "Installed virtual package placeholder: $packageName")
    }

    private fun parseArchiveInfo(pm: PackageManager, apkFile: File): PackageInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
        }
    }

    private fun loadInstalledApps() {
        if (!::context.isInitialized) return
        val prefs = context.getSharedPreferences("virtual_apps", Context.MODE_PRIVATE)
        synchronized(installedApps) {
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
    }

    private fun saveInstalledApps() {
        if (!::context.isInitialized) return
        val snapshot = getInstalledApps()
        val prefs = context.getSharedPreferences("virtual_apps", Context.MODE_PRIVATE)
        prefs.edit().apply {
            clear()
            putStringSet("packages", snapshot.map { it.packageName }.toSet())
            snapshot.forEach { app ->
                putString("${app.packageName}.label", app.label)
                putString("${app.packageName}.apkPath", app.apkPath)
                putLong("${app.packageName}.installedAt", app.installedAt)
            }
        }.apply()
    }
}

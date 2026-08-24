/*
 * Copyright (c) 2026 BalajiTechLabs & Lawnchair
 * License: Apache-2.0
 *
 * Feature Module: Shizuku Privileged System Automation
 * File: ShizukuUtils.kt
 * Description: Native Shizuku binder wrapper for 1-tap elevated system automation.
 */

package app.lawnchair.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.IBinder
import rikka.shizuku.Shizuku

object ShizukuUtils {

    private var binder: IBinder? = null

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        binder = Shizuku.getBinder()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        binder = null
    }

    val isBinderAlive: Boolean
        get() {
            if (binder?.isBinderAlive == true) return true
            return try {
                if (Shizuku.pingBinder()) {
                    binder = Shizuku.getBinder()
                    binder?.isBinderAlive == true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

    fun initialize() {
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
        } catch (e: Exception) {
            // Ignore if already registered
        }
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        if (!isBinderAlive) return false
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Exception) {
            // Request failed
        }
    }

    fun runCommand(command: String): Boolean {
        if (!hasPermission() || !isBinderAlive) return false
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, "/")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun forceEnableGoogleBubbles(context: Context): Boolean {
        if (!hasPermission() || !isBinderAlive) return false
        return try {
            runCommand("settings put secure notification_bubbles 1")
            runCommand("settings put global notification_bubbles 1")
            runCommand("cmd notification set_bubbles true")
            runCommand("pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS")
            true
        } catch (e: Exception) {
            false
        }
    }
}

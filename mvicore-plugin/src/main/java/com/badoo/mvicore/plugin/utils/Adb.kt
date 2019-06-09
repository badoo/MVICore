package com.badoo.mvicore.plugin.utils

import com.intellij.openapi.project.Project
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.io.IOException

fun forwardPort(project: Project, port: Int): Boolean {
    val adb = AndroidSdkUtils.getAdb(project)
    val bridge = AndroidSdkUtils.getDebugBridge(project)
    val error = when {
        bridge == null -> "Could not find adb."
        bridge.devices == null || bridge.devices.isEmpty() -> "No devices found."
        bridge.devices.size > 1 -> "Found too many (${bridge.devices.size}) devices."
        else -> null
    }

    if (error != null) {
        project.showError(error)
        return false
    }

    try {
        //TODO: Select device?
        val process = Runtime.getRuntime().exec("${adb?.absolutePath} reverse tcp:$port tcp:$port")
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val error = process.inputStream.bufferedReader().readText()
            project.showError(
                "Failed to forward the port:\n$error"
            )
            return false
        }
    } catch (e: IOException) {
        project.showError(
            "Failed to forward the port:", e
        )
        return false
    }

    return true
}

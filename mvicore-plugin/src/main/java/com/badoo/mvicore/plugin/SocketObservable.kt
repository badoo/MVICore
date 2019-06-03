package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.utils.showError
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import io.reactivex.Observable
import io.reactivex.ObservableSource
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.io.PrintWriter
import java.io.StringWriter
import java.net.Socket
import kotlin.concurrent.thread

class SocketObservable(
    project: Project,
    private val host: String,
    private val port: Int
): ObservableSource<JsonElement> by Observable.create<JsonElement>({ emitter ->
    val parser = JsonParser()
    if (!forwardPort(project, port)) {
        emitter.onComplete()
        return@create
    }

    val thread = thread(start = true, name = "mvicore-plugin-read") {
        try {
            val socket = Socket(host, port)
            val reader = socket.getInputStream().bufferedReader()

            while (!Thread.currentThread().isInterrupted) {
                val line = reader.readLine() ?: continue
                emitter.onNext(parser.parse(line))
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    emitter.setCancellable { thread.interrupt() }
})

private fun forwardPort(project: Project, port: Int): Boolean {
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
        bridge?.devices?.first()?.createForward(port, port)
    } catch (e: Exception) {
        project.showError(
            "Failed to forward the port:\n" + e.convertToString()
        )
        return false
    }

    return true
}

private fun Exception.convertToString(): String = StringWriter().also {
    printStackTrace(PrintWriter(it))
}.toString()

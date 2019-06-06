package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.utils.showError
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import io.reactivex.Observable
import io.reactivex.ObservableSource
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException

class SocketObservable(
    project: Project,
    private val port: Int
): ObservableSource<JsonElement> by Observable.create<JsonElement>({ emitter ->
    val parser = JsonParser()
    if (!forwardPort(project, port)) {
        emitter.onComplete()
        return@create
    }

    object : Thread( "mvicore-plugin-server") {
        override fun run() {
            val serverSocket = ServerSocket(port, 0, InetAddress.getByName("localhost"))

            emitter.setCancellable {
                serverSocket.close()
                interrupt()
            }

            while (!isInterrupted) {
                try {
                    val socket = serverSocket.accept()
                    val reader = socket.getInputStream().bufferedReader()

                    while (!socket.isClosed && socket.isConnected) {
                        val line = reader.readLine() ?: break
                        emitter.onNext(parser.parse(line))
                    }
                }
                catch (e: SocketException) {
                }
                catch (e: Exception) {
                    project.showError("Error while reading from socket:", e)
                }
            }
            serverSocket.close()
        }
    }.start()
})

private fun forwardPort(project: Project, port: Int): Boolean {
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

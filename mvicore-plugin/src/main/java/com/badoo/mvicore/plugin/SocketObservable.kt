package com.badoo.mvicore.plugin

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import io.reactivex.Observable
import io.reactivex.ObservableSource
import org.jetbrains.android.sdk.AndroidSdkUtils
import java.net.Socket
import kotlin.concurrent.thread

class SocketObservable(
    project: Project,
    private val host: String,
    private val port: Int
): ObservableSource<JsonElement> by Observable.create<JsonElement>({ emitter ->
    val parser = JsonParser()
    forwardPort(project, port)

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

private fun forwardPort(project: Project, port: Int) {
    val bridge = AndroidSdkUtils.getDebugBridge(project)
    bridge?.devices?.forEach { it.createForward(port, port) }
}

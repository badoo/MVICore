package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.utils.forwardPort
import com.badoo.mvicore.plugin.utils.showError
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import io.reactivex.Observable
import io.reactivex.ObservableSource
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

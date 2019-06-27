package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.utils.forwardPort
import com.badoo.mvicore.plugin.utils.showError
import com.badoo.mvicore.plugin.utils.stopForwarding
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
): ObservableSource<JsonElement> by Observable.create({ emitter ->
    val parser = JsonParser()
    if (!forwardPort(project, port)) {
        emitter.onComplete()
        return@create
    }

    object : Thread( "mvicore-plugin-server") {
        private var cancellables: MutableList<() -> Unit> = mutableListOf(
            { stopForwarding(project, port).ignore() }
        )

        init {
            emitter.setCancellable {
                cancellables.forEach { it() }
                interrupt()
            }
        }

        override fun run() {
            val serverSocket = ServerSocket(port, 0, InetAddress.getByName("localhost"))
            cancellables.add({ serverSocket.close() })

            while (!isInterrupted) {
                try {
                    val socket = serverSocket.accept()

                    cancellables.add(socket::close)

                    socket.getInputStream().bufferedReader().use {
                        while (!socket.isClosed && socket.isConnected) {
                            val line = it.readLine() ?: break
                            emitter.onNext(parser.parse(line))
                        }
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

private fun Any.ignore() { }

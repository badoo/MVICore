package com.badoo.mvicore.plugin

import com.badoo.mvicore.plugin.utils.forwardPort
import com.badoo.mvicore.plugin.utils.showError
import com.badoo.mvicore.plugin.utils.stopForwarding
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import java.util.Collections

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
        private var cancellables = Collections.synchronizedList(
            mutableListOf({ stopForwarding(project, port).ignore() })
        )

        init {
            emitter.setCancellable {
                interrupt()
                synchronized(cancellables) {
                    cancellables.forEach { it() }
                }
                // join() // Can affect responsiveness
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

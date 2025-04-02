package com.badoo.mvicore.middleware.socket

import com.badoo.mvicore.plugin.model.Event
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

internal class PluginSocketThread(
    private val port: Int,
    elementsCacheSize: Int,
    private val events: Observable<Event>,
    private val relay: PublishSubject<Connected> = PublishSubject.create()
) : Thread("mvicore-plugin-socket"), ObservableSource<PluginSocketThread.Connected> by relay {
    private var socket: Socket? = null
    private val gson = Gson()
    private val blockingDeque = LinkedBlockingDeque<Event>(elementsCacheSize)

    override fun run() {
        val disposable = events
            .mergeWith(
                Observable.interval(100, TimeUnit.MILLISECONDS)
                    .map { Event.Ping } // Ensure socket connection
            )
            .subscribe {
                if (socket.isActive) {
                    blockingDeque.offer(it)
                }
            }

        while (!isInterrupted) {
            try {
                socket = Socket(InetAddress.getLocalHost(), port)
                socket?.tryPing() // Shortcut fail in case it is not a real connection

                if (socket.isActive) {
                    blockingDeque.clear()
                    relay.onNext(Connected)
                }

                while (socket.isActive) {
                    val event = blockingDeque.take()
                    socket?.sendEvent(event)
                }
            } catch (e: IOException) {
                sleep(100)
            }
        }

        disposable.dispose()
    }

    private fun Socket.sendEvent(event: Event) {
        try {
            val eventString = gson.toJson(event) + "\n"
            getOutputStream().write(eventString.toByteArray())
        } catch (e: Exception) {
            throw e
        }
    }

    private fun Socket.tryPing() {
        sendEvent(Event.Ping)
    }

    private inline val Socket?.isActive
        get() = this != null && isConnected && !isClosed

    object Connected
}

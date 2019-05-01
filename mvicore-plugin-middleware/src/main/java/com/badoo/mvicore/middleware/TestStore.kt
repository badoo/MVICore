package com.badoo.mvicore.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.middleware.model.ConnectionData
import com.badoo.mvicore.middleware.model.Element
import com.badoo.mvicore.middleware.model.Event
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentLinkedDeque

object TestStore: PluginMiddleware.EventStore {
    private val events = PublishSubject.create<Event>()
    private val thread = ServerThread(events).apply { start() }

    override fun onBind(connection: Connection<out Any, out Any>) {
        events.onNext(
            Event.Bind(ConnectionData(connection))
        )
    }

    override fun <T: Any> onElement(connection: Connection<out Any, out Any>, element: T) {
        events.onNext(
            Event.Data(
                connection = ConnectionData(connection),
                element = Element(elementGson.toJsonTree(element, element.javaClass))
            )
        )
    }

    override fun onComplete(connection: Connection<out Any, out Any>) {
        events.onNext(
            Event.Complete(ConnectionData(connection))
        )
    }

    class ServerThread(private val source: Observable<Event>): Thread(ThreadGroup("plugin"), "mvicore-plugin-server") {
        private lateinit var socket: ServerSocket
        private val events = ConcurrentLinkedDeque<Event>()

        override fun run() {
            socket = ServerSocket(7675, 0, InetAddress.getLocalHost())

            source.subscribe {
                events.offer(it)
            }

            while (!isInterrupted) {
                val reader = socket.accept()
                println("Connected")
                try {
                    while (reader.isConnected) {
                        if (!events.isEmpty()) {
                            val event = events.poll()
                            println("Sending event $event")
                            try {
                                val eventString = eventGson.toJson(event) + "\n"
                                reader.getOutputStream().write(eventString.toByteArray())
                            } catch (e: Exception) {
                                events.addFirst(event)
                                throw e
                            }
                        }
                    }
                } catch (e: IOException) {
                    println("Disconnected")
                }
            }
        }
    }

    private val elementGson = GsonBuilder()
        .setFieldNamingStrategy {
            "${it.name} ${it.type}"
        }
        .create()

    private val eventGson = Gson()
}

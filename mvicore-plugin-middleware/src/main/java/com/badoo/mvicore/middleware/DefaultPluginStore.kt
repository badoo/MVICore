package com.badoo.mvicore.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.middleware.gc.QueueWatcher
import com.badoo.mvicore.middleware.gson.MviPluginTypeAdapterFactory
import com.badoo.mvicore.middleware.model.ConnectionData
import com.badoo.mvicore.middleware.model.Event
import com.badoo.mvicore.middleware.socket.PluginSocketThread
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.ref.ReferenceQueue
import java.util.ArrayDeque

class DefaultPluginStore(private val name: String, port: Int = 7675): PluginMiddleware.EventStore {
    private val events = PublishSubject.create<Event>()
    private val socket = PluginSocketThread(port, events)
    private val queueWatcher = QueueWatcher(ReferenceQueue(), ::connectionComplete)

    private val typeAwareGson = GsonBuilder()
        .registerTypeAdapterFactory(MviPluginTypeAdapterFactory())
        .create()

    private val activeConnections = mutableListOf<ConnectionData>()
    private val lastElements = ArrayDeque<Event.Data>(512)

    init {
        Observable.wrap(socket)
            .observeOn(Schedulers.single())
            .subscribe { onSocketEvent(it) }

        socket.start()
        queueWatcher.start()
    }

    override fun onBind(connection: Connection<out Any, out Any>) {
        val data = ConnectionData(connection)
        activeConnections += data
        events.onNext(Event.Bind(data))

        if (connection.from == null) {
            queueWatcher.add(connection, data)
        }
    }

    override fun <T: Any> onElement(connection: Connection<out Any, out Any>, element: T) {
        val event = Event.Data(
            connection = ConnectionData(connection),
            element = typeAwareGson.toJsonTree(element)
        )
        lastElements.add(event)

        if (lastElements.size > 512) {
            lastElements.removeFirst()
        }

        events.onNext(event)
    }

    override fun onComplete(connection: Connection<out Any, out Any>) {
        val event = ConnectionData(connection)
        connectionComplete(event)
    }

    private fun connectionComplete(event: ConnectionData) {
        activeConnections -= event
        events.onNext(Event.Complete(event))
    }

    private fun onSocketEvent(event: PluginSocketThread.Connected) {
        events.onNext(Event.Connect(name))
        reportActiveConnections()
        sendLastElements()
    }

    private fun reportActiveConnections() {
        activeConnections.forEach {
            events.onNext(Event.Bind(it))
        }
    }

    private fun sendLastElements() {
        lastElements.forEach {
            events.onNext(it)
        }
    }
}

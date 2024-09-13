package com.badoo.mvicore.middleware

import com.badoo.binder.Connection
import com.badoo.mvicore.middleware.data.parse
import com.badoo.mvicore.middleware.gc.QueueWatcher
import com.badoo.mvicore.middleware.gson.MviPluginTypeAdapterFactory
import com.badoo.mvicore.middleware.gson.SuperclassExclusionStrategy
import com.badoo.mvicore.middleware.socket.PluginSocketThread
import com.badoo.mvicore.plugin.model.ConnectionData
import com.badoo.mvicore.plugin.model.Event
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArrayList

class DefaultPluginStore(
    private val name: String,
    port: Int = 7675,
    private val elementsCacheSize: Int = 512,
    private val disposables: CompositeDisposable = CompositeDisposable(),
    ignoreOnSerialization: (Any?) -> Boolean = { false }
): IdeaPluginMiddleware.EventStore, Disposable by disposables {
    private val events = PublishSubject.create<Event>()
    private val socket = PluginSocketThread(port, elementsCacheSize * 2, events)
    private val queueWatcher = QueueWatcher(ReferenceQueue(), ::connectionComplete)

    private val typeAdapterFactory = MviPluginTypeAdapterFactory(ignoreOnSerialization)
    private val typeAwareGson = GsonBuilder()
        .registerTypeAdapterFactory(typeAdapterFactory)
        .setExclusionStrategies(SuperclassExclusionStrategy())
        .create()

    private val activeConnections = CopyOnWriteArrayList<ConnectionData>()
    private val lastElements = ConcurrentLinkedDeque<Event.Item>()

    init {
        disposables += Observable.wrap(socket)
            .observeOn(Schedulers.single())
            .subscribe { onSocketEvent(it) }

        socket.start()
        queueWatcher.start()
    }

    override fun onBind(connection: Connection<out Any, out Any>) {
        runInBackground(connection) { connection ->
            val data = connection.parse()
            activeConnections += data
            events.onNext(Event.Bind(data))

            if (connection.from == null) {
                queueWatcher.add(connection, data)
            }
        }
    }

    override fun <T: Any> onElement(connection: Connection<out Any, out Any>, element: T) {
        runInBackground(connection to element) { (connection, element) ->
            val event = Event.Item(
                connection = connection.parse(),
                element = typeAwareGson.toJsonTree(element)
            )
            lastElements.add(event)

            if (lastElements.size > elementsCacheSize) {
                lastElements.removeFirst()
            }

            events.onNext(event)
        }
    }

    override fun onComplete(connection: Connection<out Any, out Any>) {
        runInBackground(connection) { connection ->
            val event = connection.parse()
            connectionComplete(event)
        }
    }

    private fun connectionComplete(event: ConnectionData) {
        activeConnections -= event
        events.onNext(Event.Complete(event))
    }

    private fun onSocketEvent(event: PluginSocketThread.Connected) {
        events.onNext(Event.Connect(name))
        events.onNext(Event.Init(
            activeConnections.toList(),
            lastElements.toList()
        ))
    }

    private fun <T : Any> runInBackground(element: T, block: (T) -> Unit) {
        disposables += Single.just(element)
            .observeOn(Schedulers.computation())
            .subscribe(block) {
                // TODO: log?
            }
    }
}

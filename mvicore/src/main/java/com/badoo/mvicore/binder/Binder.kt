package com.badoo.mvicore.binder

import com.badoo.mvicore.binder.lifecycle.Lifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.END
import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import com.badoo.mvicore.consumer.wrap
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign

class Binder(
    private val lifecycle: Lifecycle? = null
) : Disposable {
    private val disposables = CompositeDisposable()
    private val connections = mutableListOf<Pair<Connection<out Any>, ConsumerMiddleware<out Any>?>>()
    private val connectionDisposables = CompositeDisposable()
    private var isActive = false

    init {
        lifecycle?.apply {
            disposables += Observable.wrap(this)
                .subscribe {
                    when (it) {
                       BEGIN -> bindConnections()
                       END -> unbindConnections()
                    }
                }
        }

        disposables += connectionDisposables
    }

    fun <T : Any> bind(connection: Pair<ObservableSource<out T>, Consumer<T>>) {
        bind(Connection(
            from = connection.first,
            to = connection.second
        ))
    }

    fun <T : Any> bind(connection: Connection<T>) {
        val consumer = connection.to
        val middleware = consumer.wrap(
            standalone = false,
            name = connection.name
        ) as? ConsumerMiddleware<T>

        middleware?.onBind(connection)

        when {
            lifecycle != null -> {
                connections += (connection to middleware)
                if (isActive) {
                    subscribeWithLifecycle(connection, middleware)
                }
            }
            else -> subscribe(connection, middleware)
        }
    }

    private fun <T : Any> subscribeWithLifecycle(
        connection: Connection<T>,
        middleware: ConsumerMiddleware<T>?
    ) {
        connectionDisposables += Observable
            .wrap(connection.from)
            .subscribeWithMiddleware(connection, middleware)
    }

    private fun <T: Any> subscribe(
        connection: Connection<T>,
        middleware: ConsumerMiddleware<T>?
    ) {
        disposables += Observable.wrap(connection.from)
            .subscribeWithMiddleware(connection, middleware)
    }

    private fun <T: Any> Observable<out T>.subscribeWithMiddleware(
        connection: Connection<T>,
        middleware: ConsumerMiddleware<T>?
    ): Disposable = run {
        middleware?.let {
            this.doOnNext { middleware.onElement(connection, it) }
                .doFinally { middleware.onComplete(connection) }
        } ?: this
    }.subscribe(middleware ?: connection.to)

    private fun bindConnections() {
        isActive = true
        connections.forEach { (connection, middleware) ->
            subscribeWithLifecycle(
                connection as Connection<Any>,
                middleware as? ConsumerMiddleware<Any>
            )
        }
    }

    private fun unbindConnections() {
        isActive = false
        connectionDisposables.clear()
    }

    override fun isDisposed(): Boolean =
        disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }

    fun clear() {
        disposables.clear()
    }
}




package com.badoo.mvicore.binder

import com.badoo.mvicore.binder.lifecycle.Lifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.END
import com.badoo.mvicore.consumer.middleware.base.Middleware
import com.badoo.mvicore.consumer.wrapWithMiddleware
import com.badoo.mvicore.extension.mapNotNull
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
    private val connections = mutableListOf<Pair<Connection<*, *>, Middleware<*, *>?>>()
    private val connectionDisposables = CompositeDisposable()
    private var isActive = false

    init {
        lifecycle?.let {
            disposables += it.setupConnections()
        }

        disposables += connectionDisposables
    }

    // region bind

    fun <T: Any> bind(connection: Pair<ObservableSource<T>, Consumer<T>>) {
        bind(
            Connection(
                from = connection.first,
                to = connection.second,
                transformer = { it }
            )
        )
    }

    fun <Out: Any, In: Any> bind(connection: Connection<Out, In>) {
        val consumer = connection.to
        val middleware = consumer.wrapWithMiddleware(
            standalone = false,
            name = connection.name
        ) as? Middleware<Out, In>

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

    private fun <Out: Any, In: Any> subscribeWithLifecycle(
        connection: Connection<Out, In>,
        middleware: Middleware<Out, In>?
    ) {
        connectionDisposables += Observable
            .wrap(connection.from)
            .subscribeWithMiddleware(connection, middleware)
    }

    private fun <Out: Any, In: Any> subscribe(
        connection: Connection<Out, In>,
        middleware: Middleware<Out, In>?
    ) {
        disposables += Observable.wrap(connection.from)
            .subscribeWithMiddleware(connection, middleware)
    }

    private fun <Out: Any, In: Any> Observable<Out>.subscribeWithMiddleware(
        connection: Connection<Out, In>,
        middleware: Middleware<Out, In>?
    ): Disposable =
        applyTransformer(connection)
            .run {
                middleware?.let {
                    middleware.onBind(connection)

                    this
                        .doOnNext { middleware.onElement(connection, it) }
                        .doFinally { middleware.onComplete(connection) }
                        .subscribe(middleware)

                } ?: subscribe(connection.to)
            }

    private fun <Out: Any, In: Any> Observable<Out>.applyTransformer(
        connection: Connection<Out, In>
    ): Observable<In> =
        mapNotNull(connection.transformer ?: { it as? In })

    // endregion

    // region lifecycle

    private fun Lifecycle.setupConnections() =
        Observable.wrap(this)
            .distinctUntilChanged()
            .subscribe {
                when (it) {
                    BEGIN -> bindConnections()
                    END -> unbindConnections()
                }
            }

    private fun bindConnections() {
        isActive = true
        connections.forEach { (connection, middleware) ->
            subscribeWithLifecycle(
                connection as Connection<Any, Any>,
                middleware as? Middleware<Any, Any>
            )
        }
    }

    private fun unbindConnections() {
        isActive = false
        connectionDisposables.clear()
    }

    // endregion

    override fun isDisposed(): Boolean =
        disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }

    fun clear() {
        disposables.clear()
    }
}

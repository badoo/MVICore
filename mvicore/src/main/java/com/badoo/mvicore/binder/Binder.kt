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
            lifecycle != null -> bindWithLifecycle(lifecycle, connection, middleware)
            else -> bind(connection, middleware)
        }
    }

    private fun <T : Any> bindWithLifecycle(
        lifecycle: Lifecycle,
        connection: Connection<T>,
        middleware: ConsumerMiddleware<T>?
    ) {
        val lifecycleObservable = Observable.wrap(lifecycle)
        disposables += lifecycleObservable
            .filter { it == BEGIN }
            .subscribe { _ ->
                disposables += Observable
                    .wrap(connection.from)
                    .takeUntil(lifecycleObservable.filter { it == END })
                    .subscribeWithMiddleware(connection, middleware)
            }
    }

    private fun <T: Any> bind(
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

    override fun isDisposed(): Boolean =
        disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }

    fun clear() {
        disposables.clear()
    }
}




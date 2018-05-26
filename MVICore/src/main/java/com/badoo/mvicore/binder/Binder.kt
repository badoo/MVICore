package com.badoo.mvicore.binder

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import com.badoo.mvicore.consumer.wrap
import com.badoo.mvicore.binder.lifecycle.Lifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.END
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class Binder(
    private val lifecycle: Lifecycle? = Lifecycle.indeterminate()
) : Disposable {
    private val disposables = CompositeDisposable()

    fun <T : Any> bind(connection: Pair<ObservableSource<out T>, Consumer<T>>) {
        bind(Connection(
            from = connection.first,
            to = connection.second
        ))
    }

    fun <T : Any> bind(connection: Connection<T>) {
        val source = connection.from
        val consumer = connection.to
        val middleware = consumer.wrap(
            standalone = false,
            name = connection.name
        ) as? ConsumerMiddleware<T>

        middleware?.onBind(connection)
        disposables.add(
            Observable
                .wrap(source)
                .takeUntil(Observable.wrap(lifecycle).filter { it == END } )
                .apply {
                    middleware?.let {
                        this
                            .doOnNext { middleware.onElement(connection, it) }
                            .doOnComplete { middleware.onComplete(connection) }
                            .doOnDispose { middleware.onComplete(connection) }
                    }
                }
                .subscribe(middleware ?: consumer)
        )
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




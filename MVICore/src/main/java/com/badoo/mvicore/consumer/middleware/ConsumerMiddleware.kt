package com.badoo.mvicore.consumer.middleware

import com.badoo.mvicore.binder.Connection
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

abstract class ConsumerMiddleware<T : Any>(
    protected val wrapped: Consumer<T>
) : Consumer<T>, Disposable {

    protected val innerMost = innerMost()
    private var standaloneConnection: Connection<T>? = null
    private var standaloneConnectionDisposed: Boolean = false

    private fun innerMost(): Consumer<T> {
        var current = wrapped
        while (current is ConsumerMiddleware<T>) {
            current = current.wrapped
        }

        return current
    }

    fun initAsStandalone(name: String? = null) {
        standaloneConnection = Connection(
            to = innerMost,
            name = name ?: "${innerMost.javaClass.canonicalName}.input"
        ).also {
            onBind(it)
        }
    }

    private fun check(connection: Connection<T>) {
        if (standaloneConnection != null && connection != standaloneConnection) {
            throw IllegalStateException("Middleware was initialised in standalone mode, can't accept other connections")
        }
    }

    open fun onBind(connection: Connection<T>) {
        check(connection)
        if (wrapped is ConsumerMiddleware) {
            wrapped.onBind(connection)
        }
    }

    override fun accept(t: T) {
        standaloneConnection?.let { onElement(it, t) }
        innerMost.accept(t)
    }

    open fun onElement(connection: Connection<T>, element: T) {
        check(connection)
        if (wrapped is ConsumerMiddleware) {
            wrapped.onElement(connection, element)
        }
    }

    open fun onComplete(connection: Connection<T>) {
        check(connection)
        if (wrapped is ConsumerMiddleware) {
            wrapped.onComplete(connection)
        }
    }

    override fun dispose() {
        standaloneConnection?.let {
            onComplete(it)
            standaloneConnectionDisposed = true
        }
    }

    override fun isDisposed(): Boolean =
        standaloneConnection == null || standaloneConnectionDisposed
}

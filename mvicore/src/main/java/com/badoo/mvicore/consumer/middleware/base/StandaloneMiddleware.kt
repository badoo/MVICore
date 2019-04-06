package com.badoo.mvicore.consumer.middleware.base

import com.badoo.mvicore.binder.Connection
import io.reactivex.disposables.Disposable

internal class StandaloneMiddleware<Out, In>(
    private val wrappedMiddleware: Middleware<Out, In>,
    name: String? = null,
    postfix: String? = null
): Middleware<Out, In>(wrappedMiddleware), Disposable {

    private var bound = false
    private var disposed = false
    private val connection = Connection<Out, In>(
        to = innerMost,
        name = "${name ?: innerMost.javaClass.canonicalName}.${postfix ?: "input"}"
    )

    init {
        onBind(connection)
    }

    override fun onBind(connection: Connection<Out, In>) {
        assertSame(connection)

        bound = true
        wrappedMiddleware.onBind(connection)
    }

    override fun accept(element: In) {
        wrappedMiddleware.onElement(connection, element)
        wrappedMiddleware.accept(element)
    }

    override fun onComplete(connection: Connection<Out, In>) {
        wrappedMiddleware.onComplete(connection)
    }

    override fun isDisposed() = disposed

    override fun dispose() {
        onComplete(this.connection)
        disposed = true
    }

    // todo decide about it
    protected fun finalize() {
        dispose()
    }

    private fun assertSame(connection: Connection<Out, In>) {
        if (bound && connection != this.connection) {
            throw IllegalStateException("Middleware was initialised in standalone mode, can't accept other connections")
        }
    }

}

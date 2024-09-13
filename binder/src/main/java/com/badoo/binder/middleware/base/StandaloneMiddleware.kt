package com.badoo.binder.middleware.base

import com.badoo.binder.Connection
import io.reactivex.rxjava3.disposables.Disposable

internal class StandaloneMiddleware<In : Any>(
    private val wrappedMiddleware: Middleware<In, In>,
    name: String? = null,
    postfix: String? = null
): Middleware<In, In>(wrappedMiddleware), Disposable {

    private var bound = false
    private var disposed = false
    private val connection = Connection<In, In>(
        to = innerMost,
        name = "${name ?: innerMost.javaClass.canonicalName}.${postfix ?: "input"}"
    )

    init {
        onBind(connection)
    }

    override fun onBind(connection: Connection<In, In>) {
        assertSame(connection)

        bound = true
        wrappedMiddleware.onBind(connection)
    }

    override fun accept(element: In) {
        wrappedMiddleware.onElement(connection, element)
        wrappedMiddleware.accept(element)
    }

    override fun onComplete(connection: Connection<In, In>) {
        wrappedMiddleware.onComplete(connection)
    }

    override fun isDisposed() = disposed

    override fun dispose() {
        onComplete(this.connection)
        disposed = true
    }

    private fun assertSame(connection: Connection<In, In>) {
        if (bound && connection != this.connection) {
            throw IllegalStateException("Middleware was initialised in standalone mode, can't accept other connections")
        }
    }

}

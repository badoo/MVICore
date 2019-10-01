package com.badoo.mvicore.common.middleware

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.binder.Connection

internal class StandaloneMiddleware<In>(
    private val wrappedMiddleware: Middleware<In, In>,
    name: String? = null,
    postfix: String? = null
): Middleware<In, In>(wrappedMiddleware), Cancellable {
    private val connection = Connection<In, In>(
        to = innerMost,
        name = "${name ?: innerMost::class.qualifiedName}.${postfix ?: "input"}" // FIXME wtf
    )

    init {
        onBind(connection)
    }

    override fun onBind(connection: Connection<In, In>) {
        assertSame(connection)
        wrappedMiddleware.onBind(connection)
    }

    override fun invoke(value: In) {
        wrappedMiddleware.onElement(connection, value)
        wrappedMiddleware.invoke(value)
    }

    override fun onComplete(connection: Connection<In, In>) {
        wrappedMiddleware.onComplete(connection)
    }

    override fun cancel() {
        onComplete(this.connection)
    }

    private fun assertSame(connection: Connection<In, In>) {
        if (connection !== this.connection) {
            throw IllegalStateException("Middleware was initialised in standalone mode, can't accept other connections")
        }
    }

}

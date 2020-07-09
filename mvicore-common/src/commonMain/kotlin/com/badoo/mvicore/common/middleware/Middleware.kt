package com.badoo.mvicore.common.middleware

import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.binder.Connection

abstract class Middleware<Out, In>(
    protected val wrapped: Sink<In>
): Sink<In> {

    open fun onBind(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware { onBind(connection) }
    }

    override fun accept(value: In) {
        wrapped.accept(value)
    }

    open fun onElement(connection: Connection<Out, In>, element: In) {
        wrapped.applyIfMiddleware { onElement(connection, element) }
    }

    open fun onComplete(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware { onComplete(connection) }
    }

    private inline fun Sink<In>.applyIfMiddleware(
        block: Middleware<Out, In>.() -> Unit
    ) {
        if (this is Middleware<*, *>) {
            (this as Middleware<Out, In>).block()
        }
    }

    protected val innerMost by lazy {
        var sink = wrapped
        while (sink is Middleware<*, *>) {
            sink = (sink as Middleware<Out, In>).wrapped
        }
        sink
    }
}

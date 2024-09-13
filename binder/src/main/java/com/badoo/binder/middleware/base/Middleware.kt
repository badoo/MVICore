package com.badoo.binder.middleware.base

import com.badoo.binder.Connection
import io.reactivex.rxjava3.functions.Consumer

abstract class Middleware<Out : Any, In : Any>(
    protected val wrapped: Consumer<In>
): Consumer<In> {

    open fun onBind(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware { onBind(connection) }
    }

    override fun accept(t: In) {
        wrapped.accept(t)
    }

    open fun onElement(connection: Connection<Out, In>, element: In) {
        wrapped.applyIfMiddleware { onElement(connection, element) }
    }

    open fun onComplete(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware { onComplete(connection) }
    }

    private inline fun Consumer<In>.applyIfMiddleware(
        block: Middleware<Out, In>.() -> Unit
    ) {
        if (this is Middleware<*, *>) {
            (this as Middleware<Out, In>).block()
        }
    }

    protected val innerMost by lazy {
        var consumer = wrapped
        while (consumer is Middleware<*, *>) {
            consumer = (consumer as Middleware<Out, In>).wrapped
        }
        consumer
    }
}

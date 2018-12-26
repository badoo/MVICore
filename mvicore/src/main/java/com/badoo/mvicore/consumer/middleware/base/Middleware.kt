package com.badoo.mvicore.consumer.middleware.base

import com.badoo.mvicore.binder.Connection
import io.reactivex.functions.Consumer

abstract class Middleware<Out, In>(
    protected val wrapped: Consumer<In>
): Consumer<In> {

    open fun onBind(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware { onBind(connection) }
    }

    override fun accept(element: In) {
        wrapped.accept(element)
    }

    open fun onElement(connection: Connection<Out, In>, element: In) {
        wrapped.applyIfMiddleware { onElement(connection, element) }
    }

    open fun onComplete(connection: Connection<Out, In>) {
        wrapped.applyIfMiddleware { onBind(connection) }
    }

    private inline fun Consumer<In>.applyIfMiddleware(
        block: Middleware<Out, In>.() -> Unit
    ) {
        if (wrapped is Middleware<*, *>) {
            (wrapped as Middleware<Out, In>).block()
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

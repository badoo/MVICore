package com.badoo.mvicore.consumer

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import io.reactivex.functions.Consumer

typealias ConsumerMiddlewareFactory<T> = (Consumer<T>) -> ConsumerMiddleware<T>

object Middlewares {
    val forAll: MutableList<ConsumerMiddlewareFactory<*>> = mutableListOf()
    val forNamed: MutableList<ConsumerMiddlewareFactory<*>> = mutableListOf()
}

fun <T : Any> Consumer<T>.wrap(
    standalone: Boolean = true,
    name: String? = null
): Consumer<T> {
    var current = this
    val additional = if (standalone || name != null) Middlewares.forNamed else null
    val middlewares = Middlewares.forAll + (additional ?: emptyList())

    middlewares.forEach {
        current = (it.invoke(current) as ConsumerMiddleware<T>)
    }

    if (current is ConsumerMiddleware<T> && standalone) {
        (current as ConsumerMiddleware<T>).initAsStandalone(name)
    }

    return current
}

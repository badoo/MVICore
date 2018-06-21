package com.badoo.mvicore.consumer

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import com.badoo.mvicore.consumer.middlewareconfig.Middlewares
import com.badoo.mvicore.consumer.middlewareconfig.NonWrappable
import io.reactivex.functions.Consumer

fun <T : Any> Consumer<T>.wrap(
    standalone: Boolean = true,
    name: String? = null,
    postfix: String? = null,
    wrapperOf: Any? = null
): Consumer<T> {
    val target = wrapperOf ?: this
    if (target is NonWrappable) return this

    var current = this

    Middlewares.configurations.forEach {
        current = it.applyOn(current, name, standalone)
    }

    if (current is ConsumerMiddleware<T> && standalone) {
        (current as ConsumerMiddleware<T>).initAsStandalone(
            name = name,
            wrapperOf = target,
            postfix = postfix
        )
    }

    return current
}

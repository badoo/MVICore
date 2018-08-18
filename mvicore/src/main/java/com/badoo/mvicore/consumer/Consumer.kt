package com.badoo.mvicore.consumer

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import com.badoo.mvicore.consumer.middlewareconfig.Middlewares
import com.badoo.mvicore.consumer.middlewareconfig.NonWrappable
import io.reactivex.functions.Consumer

/**
 * Wraps a Consumer<T> with Middlewares. The list of Middlewares that will be applied is resolved
 * by calling on all available [com.badoo.mvicore.consumer.middlewareconfig.MiddlewareConfiguration]
 *
 * @param standalone    Whether the Consumer<T> is used as a standalone target (true), or as
 *                      part of a [com.badoo.mvicore.binder.Connection] used by [com.badoo.mvicore.binder.Binder].
 *                      Defaults to true. In most cases you should not need to override the default value.
 * @param name          The name for the wrapping. Useful to add MiddlewareConfiguration
 *                      that only applies to named wrappings, or to make them human-readable
 *                      inside logs or time-travel debugger.
 * @param postfix       Passed on to [ConsumerMiddleware], in most cases you shouldn't need to override this.
 * @param wrapperOf     Passed on to [ConsumerMiddleware], in most cases you shouldn't need to override this.
 */
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
        current = it.applyOn(current, target, name, standalone)
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

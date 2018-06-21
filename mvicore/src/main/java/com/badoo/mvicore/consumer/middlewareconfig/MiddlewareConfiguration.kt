package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import io.reactivex.functions.Consumer

data class MiddlewareConfiguration(
    private val condition: WrappingCondition,
    private val factories: List<ConsumerMiddlewareFactory<*>>
) {

    fun <T : Any> applyOn(
        consumerToWrap: Consumer<T>,
        targetToCheck: Any,
        name: String?,
        standalone: Boolean
    ): Consumer<T> {
        var current = consumerToWrap
        val middlewares = if (condition.shouldWrap(targetToCheck, name, standalone)) factories else listOf()
        middlewares.forEach {
            current = (it.invoke(current) as ConsumerMiddleware<T>)
        }

        return current
    }
}

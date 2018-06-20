package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import io.reactivex.functions.Consumer

data class MiddlewareConfiguration(
    private val condition: WrappingCondition,
    private val factories: List<ConsumerMiddlewareFactory<*>>
) {

    fun <T : Any> applyOn(consumer: Consumer<T>): Consumer<T> {
        var current = consumer
        val middlewares = if (condition.shouldWrap(current)) factories else listOf()
        middlewares.forEach {
            current = (it.invoke(current) as ConsumerMiddleware<T>)
        }

        return current
    }
}

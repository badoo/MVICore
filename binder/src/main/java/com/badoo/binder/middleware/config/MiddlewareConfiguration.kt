package com.badoo.binder.middleware.config

import com.badoo.binder.middleware.base.Middleware
import io.reactivex.rxjava3.functions.Consumer

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
            current = it.invoke(current) as Middleware<Any, T>
        }

        return current
    }
}

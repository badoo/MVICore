package com.badoo.mvicore.consumer.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.consumer.util.Logger
import io.reactivex.functions.Consumer

class LoggingMiddleware<T : Any>(
    wrapped: Consumer<T>,
    private val logger: Logger
) : ConsumerMiddleware<T>(wrapped) {

    override fun onBind(connection: Connection<T>) {
        super.onBind(connection)
        logger("LoggingMiddleWare: Binding $connection")
    }

    override fun onElement(connection: Connection<T>, element: T) {
        super.onElement(connection, element)
        logger("LoggingMiddleWare: New element [$element] on $connection")
    }

    override fun onComplete(connection: Connection<T>) {
        super.onComplete(connection)
        logger("LoggingMiddleWare: Unbinding $connection")
    }
}

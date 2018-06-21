package com.badoo.mvicore.consumer.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.consumer.util.Logger
import io.reactivex.functions.Consumer
import java.util.Locale

class LoggingMiddleware<T : Any>(
    wrapped: Consumer<T>,
    private val logger: Logger,
    private val config: Config = Config()
) : ConsumerMiddleware<T>(wrapped) {

    data class Config(
        val locale: Locale = Locale.US,
        val tag: String = "LoggingMiddleware",
        val onBindTemplate: String = "Binding %s",
        val onElementTemplate: String = "New element on %s: [%s]",
        val onCompleteTemplate: String = "Unbinding %s"
    )

    private fun log(message: String) {
        logger("${config.tag}: $message")
    }

    override fun onBind(connection: Connection<T>) {
        super.onBind(connection)
        log(config.onBindTemplate.format(config.locale, connection))
    }

    override fun onElement(connection: Connection<T>, element: T) {
        super.onElement(connection, element)
        log(config.onElementTemplate.format(config.locale, connection, element))
    }

    override fun onComplete(connection: Connection<T>) {
        super.onComplete(connection)
        log(config.onCompleteTemplate.format(config.locale, connection))
    }
}

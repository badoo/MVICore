package com.badoo.mvicore.consumer.middleware

import com.badoo.binder.Connection
import com.badoo.binder.middleware.base.Middleware
import com.badoo.mvicore.consumer.util.Logger
import io.reactivex.rxjava3.functions.Consumer
import java.util.Locale

class LoggingMiddleware<Out: Any, In: Any>(
    wrapped: Consumer<In>,
    private val logger: Logger,
    private val config: Config = Config()
) : Middleware<Out, In>(wrapped) {

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

    override fun onBind(connection: Connection<Out, In>) {
        super.onBind(connection)
        log(config.onBindTemplate.format(config.locale, connection))
    }

    override fun onElement(connection: Connection<Out, In>, element: In) {
        super.onElement(connection, element)
        log(config.onElementTemplate.format(config.locale, connection, element))
    }

    override fun onComplete(connection: Connection<Out, In>) {
        super.onComplete(connection)
        log(config.onCompleteTemplate.format(config.locale, connection))
    }
}

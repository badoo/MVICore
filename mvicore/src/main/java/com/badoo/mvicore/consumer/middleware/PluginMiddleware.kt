package com.badoo.mvicore.consumer.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.consumer.middleware.Test.Event.*
import com.badoo.mvicore.consumer.middleware.base.Middleware
import io.reactivex.functions.Consumer
import java.util.*

class PluginMiddleware<Out: Any, In: Any>(
    wrapped: Consumer<In>,
    val store: PluginStore
): Middleware<Out, In>(wrapped) {

    override fun onBind(connection: Connection<Out, In>) {
        super.onBind(connection)

    }

    override fun onElement(connection: Connection<Out, In>, element: In) {
        super.onElement(connection, element)
        store.onElement(connection, element)
    }

    override fun onComplete(connection: Connection<Out, In>) {
        super.onComplete(connection)

    }

    interface PluginStore {
        fun onBind(connection: Connection<out Any, out Any>)
        fun onElement(connection: Connection<out Any, out Any>, element: Any)
        fun onComplete(connection: Connection<Any, Any>)
    }
}

object Test: PluginMiddleware.PluginStore {

    private val events = LinkedList<Event>()

    override fun onBind(connection: Connection<out Any, out Any>) {
        events.add(
            Bind(ConnectionData(connection))
        )
    }

    override fun onElement(connection: Connection<out Any, out Any>, element: Any) {
        events.add(
            Data(
                connection = ConnectionData(connection),
                element = Element(element)
            )
        )
    }

    override fun onComplete(connection: Connection<Any, Any>) {
        events.add(
            Complete(ConnectionData(connection))
        )
    }

    data class ConnectionData(
        val from: String,
        val to: String,
        val name: String?
    ) {
        constructor(connection: Connection<out Any, out Any>): this(
            connection.from.toString(),
            connection.to.toString(),
            connection.name
        )
    }

    data class Element(
        val payload: Any
    )

    sealed class Event {
        data class Bind(
            val connectionData: ConnectionData
        ): Event()

        data class Data(
            val connection: ConnectionData,
            val element: Element
        ): Event()

        data class Complete(
            val connection: ConnectionData
        ): Event()
    }
}

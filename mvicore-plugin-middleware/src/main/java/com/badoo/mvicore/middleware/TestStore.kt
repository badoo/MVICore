package com.badoo.mvicore.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.middleware.model.ConnectionData
import com.badoo.mvicore.middleware.model.Element
import com.badoo.mvicore.middleware.model.Event
import java.util.*

object TestStore: PluginMiddleware.EventStore {

    private val events = LinkedList<Event>()

    override fun onBind(connection: Connection<out Any, out Any>) {
        events.add(
            Event.Bind(ConnectionData(connection))
        )
    }

    override fun onElement(connection: Connection<out Any, out Any>, element: Any) {
        events.add(
            Event.Data(
                connection = ConnectionData(connection),
                element = Element(element)
            )
        )
    }

    override fun onComplete(connection: Connection<out Any, out Any>) {
        events.add(
            Event.Complete(ConnectionData(connection))
        )
    }
}

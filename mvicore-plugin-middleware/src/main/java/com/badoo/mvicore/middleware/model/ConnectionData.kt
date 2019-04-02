package com.badoo.mvicore.middleware.model

import com.badoo.mvicore.binder.Connection

data class ConnectionData(
    val from: String,
    val to: String,
    val name: String?
) {
    constructor(connection: Connection<out Any, out Any>) : this(
        connection.from.toString(),
        connection.to.toString(),
        connection.name
    )
}

data class Element(
    val payload: Any
)

sealed class Event(val type: String) {
    data class Bind(
        val connection: ConnectionData
    ) : Event("bind")

    data class Data(
        val connection: ConnectionData,
        val element: Element
    ) : Event("data")

    data class Complete(
        val connection: ConnectionData
    ) : Event("complete")
}

package com.badoo.mvicore.middleware.model

import com.google.gson.JsonElement

internal sealed class Event(val type: String) {
    data class Bind(
        val connection: ConnectionData
    ) : Event("bind")

    data class Data(
        val connection: ConnectionData,
        val element: JsonElement
    ) : Event("data")

    data class Complete(
        val connection: ConnectionData
    ) : Event("complete")

    data class Connect(
        val name: String
    ) : Event("connect")

    object Ping: Event("ping")
}

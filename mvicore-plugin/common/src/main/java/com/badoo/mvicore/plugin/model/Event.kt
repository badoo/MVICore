package com.badoo.mvicore.plugin.model

import com.google.gson.JsonElement

sealed class Event(val type: String) {
    data class Init(
        val connections: List<ConnectionData>,
        val items: List<Item>
    ) : Event("init")

    data class Bind(
        val connection: ConnectionData
    ) : Event("bind")

    data class Item(
        val connection: ConnectionData,
        val element: JsonElement
    ) : Event("data")

    data class Complete(
        val connection: ConnectionData
    ) : Event("complete")

    data class Connect(
        val name: String
    ) : Event("connect")

    data object Ping : Event("ping")
}

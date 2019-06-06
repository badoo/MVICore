package com.badoo.mvicore.middleware.model

import com.badoo.mvicore.binder.Connection

internal data class ConnectionData(
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

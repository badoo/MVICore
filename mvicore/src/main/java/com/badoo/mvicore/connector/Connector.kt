package com.badoo.mvicore.connector

@Deprecated(
    message = "Connector is now moved to a separate module",
    replaceWith = ReplaceWith(
        "Connector",
        "com.badoo.binder.connector.Connector"
    )
)
typealias Connector<Out, In> = com.badoo.binder.connector.Connector<Out, In>

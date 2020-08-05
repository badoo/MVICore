package com.badoo.mvicore.binder.lifecycle

@Deprecated(
    message = "Lifecycle is moved to a separate module",
    replaceWith = ReplaceWith(
        "Lifecycle",
        "com.badoo.binder.lifecycle.Lifecycle"
    )
)
typealias Lifecycle = com.badoo.binder.lifecycle.Lifecycle

@Deprecated(
    message = "Lifecycle is moved to a separate module",
    replaceWith = ReplaceWith(
        "ManualLifecycle",
        "com.badoo.binder.lifecycle.ManualLifecycle"
    )
)
typealias ManualLifecycle = com.badoo.binder.lifecycle.ManualLifecycle

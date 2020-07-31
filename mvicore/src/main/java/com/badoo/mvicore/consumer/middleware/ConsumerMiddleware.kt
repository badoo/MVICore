package com.badoo.mvicore.consumer.middleware

import com.badoo.binder.middleware.base.Middleware

@Deprecated(
    "Left for compatibility reasons",
    ReplaceWith("Middleware<Any, T>", "com.badoo.binder.middleware.base")
)
typealias ConsumerMiddleware<T> = Middleware<Any, T>

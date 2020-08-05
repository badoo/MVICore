package com.badoo.mvicore.consumer.middleware

import com.badoo.mvicore.consumer.middleware.base.Middleware

@Deprecated(
    "Left for compatibility reasons",
    ReplaceWith("Middleware<Any, T>", "com.badoo.binder.middleware.base.Middleware")
)
typealias ConsumerMiddleware<T> = Middleware<Any, T>

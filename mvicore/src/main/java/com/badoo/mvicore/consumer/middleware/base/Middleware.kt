package com.badoo.mvicore.consumer.middleware.base

import com.badoo.binder.middleware.base.Middleware

@Deprecated(
    "Left for compatibility reasons",
    ReplaceWith("Middleware<Out, In>", "com.badoo.binder.middleware.base.Middleware")
)
typealias Middleware<Out, In> = Middleware<Out, In>

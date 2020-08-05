package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.binder.middleware.config.Middlewares

@Deprecated(
    message = "Middleware is moved to a separate module",
    replaceWith = ReplaceWith(
        expression = "Middlewares",
        imports = ["com.badoo.binder.middleware.config.Middlewares"]
    )
)
typealias Middlewares = Middlewares

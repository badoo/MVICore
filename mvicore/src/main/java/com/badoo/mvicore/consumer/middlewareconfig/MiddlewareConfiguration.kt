package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.binder.middleware.config.MiddlewareConfiguration

@Deprecated(
    message = "Middleware is moved to a separate module",
    replaceWith = ReplaceWith(
        expression = "MiddlewareConfiguration",
        imports = ["com.badoo.binder.middleware.config.MiddlewareConfiguration"]
    )
)
typealias MiddlewareConfiguration = MiddlewareConfiguration

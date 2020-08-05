package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.binder.middleware.config.WrappingCondition

@Deprecated(
    message = "Middleware is moved to a separate module",
    replaceWith = ReplaceWith(
        expression = "WrappingCondition",
        imports = ["com.badoo.binder.middleware.config.WrappingCondition"]
    )
)
typealias WrappingCondition = WrappingCondition

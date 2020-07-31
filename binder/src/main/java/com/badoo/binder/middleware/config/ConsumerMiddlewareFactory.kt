package com.badoo.binder.middleware.config

import com.badoo.binder.middleware.base.Middleware
import io.reactivex.functions.Consumer

typealias ConsumerMiddlewareFactory<T> = (Consumer<T>) -> Middleware<Any, T>

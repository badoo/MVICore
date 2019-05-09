package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.mvicore.consumer.middleware.base.Middleware
import io.reactivex.functions.Consumer

typealias ConsumerMiddlewareFactory<T> = (Consumer<T>) -> Middleware<Any, T>

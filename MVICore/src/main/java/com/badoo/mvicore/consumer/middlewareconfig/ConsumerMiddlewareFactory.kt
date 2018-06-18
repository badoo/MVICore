package com.badoo.mvicore.consumer.middlewareconfig

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import io.reactivex.functions.Consumer

typealias ConsumerMiddlewareFactory<T> = (Consumer<T>) -> ConsumerMiddleware<T>

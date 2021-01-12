package com.badoo.binder.middleware.config

import com.badoo.binder.middleware.base.Middleware
import io.reactivex.rxjava3.functions.Consumer

typealias ConsumerMiddlewareFactory<T> = (Consumer<T>) -> Middleware<Any, T>

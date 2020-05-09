package com.badoo.mvicore.rx

import com.badoo.mvicore.common.Sink
import io.reactivex.functions.Consumer

fun <T> Consumer<T>.toSink() = object : Sink<T> {
    override fun accept(value: T) {
        this@toSink.accept(value)
    }
}

package com.badoo.mvicore.common

interface Sink<T> {
    operator fun invoke(value: T)
}

fun <T> sinkOf(action: (T) -> Unit) = object : Sink<T> {
    override fun invoke(value: T) {
        action(value)
    }
}

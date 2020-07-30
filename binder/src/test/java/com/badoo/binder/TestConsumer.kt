package com.badoo.binder

import io.reactivex.functions.Consumer
import kotlin.test.assertEquals

class TestConsumer<T> : Consumer<T> {
    val values = mutableListOf<T>()

    override fun accept(item: T) {
        values.add(item)
    }
}

fun <T> TestConsumer<T>.assertValues(vararg values: T) =
    assertEquals(values.toList(), this.values)

package com.badoo.binder

import io.reactivex.rxjava3.functions.Consumer
import org.junit.jupiter.api.Assertions.assertEquals

class TestConsumer<T : Any> : Consumer<T> {
    val values = mutableListOf<T>()

    override fun accept(item: T) {
        values.add(item)
    }
}

fun <T : Any> TestConsumer<T>.assertValues(vararg values: T) =
    assertEquals(values.toList(), this.values)

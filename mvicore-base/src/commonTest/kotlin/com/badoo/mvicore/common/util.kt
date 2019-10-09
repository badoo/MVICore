package com.badoo.mvicore.common

import kotlin.test.assertEquals

class TestSink<T>: Sink<T> {
    private val _values: MutableList<T> = mutableListOf()
    val values: List<T>
        get() = _values

    override fun invoke(value: T) {
        _values += value
    }
}

fun <T> TestSink<T>.assertValues(vararg values: T) =
    assertEquals(values.toList(), this.values)

fun <T> TestSink<T>.assertNoValues() =
    assertEquals(emptyList(), this.values)

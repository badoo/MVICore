package com.badoo.mvicore.common

class TestSink<T>: Sink<T> {
    private val _values: MutableList<T> = mutableListOf()
    val values: List<T>
        get() = _values

    override fun invoke(value: T) {
        _values += value
    }
}

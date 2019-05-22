package com.badoo.mvicore

class Self<T>: (T) -> T {
    override fun invoke(value: T): T = value
}

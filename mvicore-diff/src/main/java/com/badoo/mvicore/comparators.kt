package com.badoo.mvicore

typealias DiffStrategy<T> = (T?, T) -> Boolean

class ByValue<T>: DiffStrategy<T> {
    override fun invoke(p1: T?, p2: T): Boolean = p2 != p1
}

class ByRef<T>: DiffStrategy<T> {
    override fun invoke(p1: T?, p2: T): Boolean = p2 !== p1
}

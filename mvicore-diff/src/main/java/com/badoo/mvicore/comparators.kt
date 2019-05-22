package com.badoo.mvicore

private typealias Comparator<T> = (T?, T) -> Boolean

class ByValue<T>: Comparator<T> {
    override fun invoke(p1: T?, p2: T): Boolean = p2 != p1
}

class ByRef<T>: Comparator<T> {
    override fun invoke(p1: T?, p2: T): Boolean = p2 !== p1
}

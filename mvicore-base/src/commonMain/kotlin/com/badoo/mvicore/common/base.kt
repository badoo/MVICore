package com.badoo.mvicore.common

expect class AtomicRef<V>(initialValue: V) {
    fun compareAndSet(expect: V, update: V): Boolean
    fun get(): V
}

internal fun <T> AtomicRef<T>.update(update: (T) -> T) {
    do {
        val oldValue = get()
        val result = compareAndSet(oldValue, update(oldValue))
    } while (!result)
}

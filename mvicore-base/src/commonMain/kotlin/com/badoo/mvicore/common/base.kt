package com.badoo.mvicore.common

expect class AtomicRef<V : Any>(initialValue: V) {
    fun compareAndSet(expect: V, update: V): Boolean
    fun get(): V
}

internal fun <T : Any> AtomicRef<T>.update(update: (T) -> T) {
    do {
        val oldValue = get()
        val result = compareAndSet(oldValue, update(oldValue))
    } while (!result)
}

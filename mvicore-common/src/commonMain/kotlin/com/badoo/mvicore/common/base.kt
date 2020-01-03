package com.badoo.mvicore.common

expect class AtomicRef<V>(initialValue: V) {
    fun compareAndSet(expect: V, update: V): Boolean
    fun get(): V
}

internal inline fun <T> AtomicRef<T>.update(update: (T) -> T) {
    do {
        val oldValue = get()
        val result = compareAndSet(oldValue, update(oldValue))
    } while (!result)
}

internal inline operator fun <reified T> Array<T>.minus(value: T): Array<T> {
    val index = indexOf(value)
    return if (index == -1) {
        this
    } else {
        val newArray = arrayOfNulls<T>(size - 1)
        copyInto(newArray, destinationOffset = 0, startIndex = 0, endIndex = index)
        copyInto(newArray, destinationOffset = index, startIndex = index + 1, endIndex = size)
        newArray as Array<T>
    }
}

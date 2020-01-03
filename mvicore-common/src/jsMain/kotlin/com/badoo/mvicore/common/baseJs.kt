package com.badoo.mvicore.common

actual class AtomicRef<V> actual constructor(initialValue: V) {
    private var value: V = initialValue

    actual fun compareAndSet(expect: V, update: V): Boolean {
        if (value == expect) {
            value = update
            return true
        }
        return false
    }

    actual fun get(): V = value
}

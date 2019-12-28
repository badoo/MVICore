package com.badoo.mvicore.common

import kotlin.native.concurrent.FreezableAtomicReference
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen

actual class AtomicRef<V : Any> actual constructor(initialValue: V) {

    private val delegate = FreezableAtomicReference(initialValue)

    actual fun get(): V = delegate.value

    actual fun compareAndSet(expect: V, update: V): Boolean =
        delegate.compareAndSet(expect, update.freezeIfFrozen())

    private fun V.freezeIfFrozen(): V {
        if (delegate.isFrozen) {
            freeze()
        }

        return this
    }
}

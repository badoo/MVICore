package com.badoo.mvicore.common

import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.update

interface Cancellable {
    fun cancel()
    val isCancelled: Boolean
}

fun cancellableOf(onCancel: Cancellable.() -> Unit): Cancellable = CancellableImpl(onCancel)
fun cancelled(): Cancellable = CancelledCancellable()

private class CancellableImpl(private val onCancel: Cancellable.() -> Unit): Cancellable {
    private var isCancelledRef = AtomicBoolean(false)

    override val isCancelled: Boolean
        get() = isCancelledRef.value

    override fun cancel() {
        if (!isCancelled) {
            isCancelledRef.value = true
            onCancel()
        }
    }
}

private class CancelledCancellable : Cancellable {
    override val isCancelled: Boolean = true
    override fun cancel() {
        // no-op
    }
}

class CompositeCancellable(vararg cancellables: Cancellable): Cancellable {
    private val cancellableListRef: AtomicReference<Set<Cancellable>> = AtomicReference(hashSetOf(*cancellables))
    private val internalCancellable = CancellableImpl {
        val list = cancellableListRef.value
        list.forEach { it.cancel() }
        cancellableListRef.update { emptySet() }
    }

    override val isCancelled: Boolean
        get() = internalCancellable.isCancelled

    operator fun plusAssign(cancellable: Cancellable?) {
        if (!isCancelled && cancellable != null) {
            cancellableListRef.update {
                (it + cancellable).filterNotTo(hashSetOf()) { it.isCancelled }
            }
        }
    }

    operator fun minusAssign(cancellable: Cancellable?) {
        if (!isCancelled && cancellable != null) {
            cancellableListRef.update { it - cancellable }
        }
    }

    override fun cancel() {
        internalCancellable.cancel()
    }
}

internal fun AtomicReference<Cancellable?>.cancel() {
    val value = value
    if (value != null) {
        value.cancel()
        compareAndSet(value, null)
    }
}

package com.badoo.mvicore.common

interface Cancellable {
    fun cancel()
    val isCancelled: Boolean
}

fun cancellableOf(onCancel: Cancellable.() -> Unit): Cancellable = CancellableImpl(onCancel)
fun cancelled(): Cancellable = CancelledCancellable()

private class CancellableImpl(private val onCancel: Cancellable.() -> Unit): Cancellable {
    private var isCancelledRef = AtomicRef(false)

    override val isCancelled: Boolean
        get() = isCancelledRef.get()

    override fun cancel() {
        if (!isCancelled) {
            isCancelledRef.update { true }
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
    private val cancellableListRef: AtomicRef<Set<Cancellable>> = AtomicRef(hashSetOf(*cancellables))
    private val internalCancellable = CancellableImpl {
        val list = cancellableListRef.get()
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

internal fun AtomicRef<Cancellable?>.cancel() {
    val value = get()
    if (value != null) {
        value.cancel()
        compareAndSet(value, null)
    }
}

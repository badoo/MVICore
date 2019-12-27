package com.badoo.mvicore.common

interface Cancellable {
    fun cancel()
    val isCancelled: Boolean
}

fun cancellableOf(onCancel: Cancellable.() -> Unit): Cancellable = CancellableImpl(onCancel)

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

class CompositeCancellable(vararg cancellables: Cancellable): Cancellable {
    private val cancellableList = mutableListOf(*cancellables)
    private val internalCancellable = CancellableImpl {
        cancellableList.forEach { it.cancel() }
        cancellableList.clear()
    }

    override val isCancelled: Boolean
        get() = internalCancellable.isCancelled

    operator fun plusAssign(cancellable: Cancellable?) {
        if (!isCancelled) {
            cancellableList.removeAll { it.isCancelled }
            cancellable?.let {
                cancellableList += cancellable
            }
        }
    }

    operator fun minusAssign(cancellable: Cancellable?) {
        if (!isCancelled) {
            cancellableList.remove(cancellable)
        }
    }

    override fun cancel() {
        internalCancellable.cancel()
    }
}

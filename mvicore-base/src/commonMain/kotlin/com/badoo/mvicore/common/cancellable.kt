package com.badoo.mvicore.common

interface Cancellable {
    fun cancel()
}

fun cancellableOf(onCancel: () -> Unit): Cancellable = CancellableImpl(onCancel)

private class CancellableImpl(private val onCancel: () -> Unit): Cancellable {
    private var isCancelledRef = AtomicRef(false)

    val isCancelled: Boolean = isCancelledRef.get()
    override fun cancel() {
        if (!isCancelled) {
            isCancelledRef.update { true }
            onCancel()
        }
    }
}

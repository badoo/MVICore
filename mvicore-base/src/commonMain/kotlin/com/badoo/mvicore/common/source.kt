package com.badoo.mvicore.common

/**
 * NOTE: in conversions from other frameworks you need to override equals and hashCode
 * to support binder "emit after dispose" functionality
 */
interface Source<T> : Cancellable {
    fun connect(sink: Sink<T>): Cancellable
}

fun <T> source(initialValue: T): SimpleSource<T> =
    SimpleSource(initialValue, true)

fun <T> source(): SimpleSource<T> =
    SimpleSource(null, false)

class SimpleSource<T>(initialValue: T?, private val emitOnConnect: Boolean): Source<T>, Sink<T> {
    private val sinks = AtomicRef(listOf<Sink<T>>())
    private val internalCancellable = CompositeCancellable(
        cancellableOf { sinks.update { emptyList() } }
    )
    var value: T? = initialValue
        private set

    override fun invoke(value: T) {
        this.value = value
        val sinks = sinks.get()
        sinks.forEach { it.invoke(value) }
    }

    override fun connect(sink: Sink<T>): Cancellable {
        sinks.update { it + sink }

        val value = value
        if (emitOnConnect && value != null) {
            sink.invoke(value)
        }

        return cancellableOf {
            sinks.update { it - sink }
            internalCancellable -= this
        }.also {
            internalCancellable += it
        }
    }

    override val isCancelled: Boolean
        get() = internalCancellable.isCancelled

    override fun cancel() {
        internalCancellable.cancel()
    }
}

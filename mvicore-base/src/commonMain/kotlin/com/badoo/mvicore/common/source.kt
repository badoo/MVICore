package com.badoo.mvicore.common

/**
 * NOTE: in conversions from other frameworks you need to override equals and hashCode
 * to support binder "emit after dispose" functionality
 */
interface Source<T> {
    fun connect(sink: Sink<T>): Cancellable
}

fun <T> source(initialValue: T): SourceImpl<T> =
    SourceImpl(initialValue, true)

fun <T> source(): SourceImpl<T> =
    SourceImpl(null, false)

fun <T> Source<T>.connect(action: (T) -> Unit) =
    connect(sinkOf(action))

class SourceImpl<T>(initialValue: T?, private val emitOnConnect: Boolean): Source<T>, Sink<T> {
    private val sinks = AtomicRef(listOf<Sink<T>>())
    private val _value = AtomicRef(initialValue)
    val value: T?
        get() = _value.get()

    override fun invoke(value: T) {
        _value.update { value }
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
        }
    }

//    override val isCancelled: Boolean
//        get() = internalCancellable.isCancelled
//
//    override fun cancel() {
//        internalCancellable.cancel()
//    }
}

package com.badoo.mvicore.common

import com.badoo.reaktive.utils.atomic.AtomicReference

interface Sink<in T> {
    fun accept(value: T)
}

interface Observer<in T> : Sink<T> {
    fun onSubscribe(cancellable: Cancellable)
    fun onComplete()
    fun onError(throwable: Throwable)
}

fun <T> sinkOf(action: (T) -> Unit): Sink<T> = SinkFromAction(action)

private class SinkFromAction<in T>(private val action: (T) -> Unit): Sink<T> {
    override fun accept(value: T) {
        action(value)
    }
}

fun <T> Sink<T>.toObserver(): Observer<T> = ObserverFromSink(this)

private class ObserverFromSink<in T>(private val sink: Sink<T>): Observer<T> {
    private val cancellable: AtomicReference<Cancellable?> = AtomicReference(null)

    override fun accept(value: T) {
        sink.accept(value)
    }

    override fun onSubscribe(cancellable: Cancellable) {
        this.cancellable.compareAndSet(null, cancellable)
    }

    override fun onComplete() {
        cancellable.cancel()
    }

    override fun onError(throwable: Throwable) {
        cancellable.cancel()
        throw throwable
    }
}

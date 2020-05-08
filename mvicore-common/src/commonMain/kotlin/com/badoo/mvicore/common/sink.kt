package com.badoo.mvicore.common

import com.badoo.reaktive.utils.atomic.AtomicReference

interface Sink<T> {
    operator fun invoke(value: T)
}

interface Observer<T> : Sink<T> {
    fun onSubscribe(cancellable: Cancellable)
    fun onComplete()
    fun onError(throwable: Throwable)
}

fun <T> sinkOf(action: (T) -> Unit): Sink<T> = SinkFromAction(action)

private class SinkFromAction<T>(private val action: (T) -> Unit): Sink<T> {
    override fun invoke(value: T) {
        action(value)
    }
}

fun <T> ((T) -> Unit).toObserver(): Observer<T> =
    ObserverFromAction(this)

private class ObserverFromAction<T>(val action: (T) -> Unit): Observer<T> {
    private val cancellable: AtomicReference<Cancellable?> = AtomicReference(null)

    override fun invoke(value: T) {
        action(value)
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

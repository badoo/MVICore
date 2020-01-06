package com.badoo.mvicore.common

/**
 * NOTE: in conversions from other frameworks you need to override equals and hashCode
 * to support binder "emit after dispose" functionality
 */
interface Source<T> {
    fun connect(observer: Observer<T>): Cancellable
}

fun <T> source(initialValue: T): BehaviourSource<T> = BehaviourSource(initialValue)

fun <T> source(): PublishSource<T> = PublishSource()

fun <T> Source<T>.connect(action: (T) -> Unit) =
    connect(action.toObserver())

fun <T> Source<T>.connect(sink: Sink<T>) =
    connect(sink::invoke)


class PublishSource<T> internal constructor(): Source<T>, Sink<T> {
    private val observers: AtomicRef<List<Observer<T>>> = AtomicRef(emptyList())

    override fun connect(observer: Observer<T>): Cancellable {
        observers.update { it + observer }
        val cancellable = cancellableOf {
            observers.update { it - observer }
            observer.onComplete()
        }

        observer.onSubscribe(cancellable)
        return cancellable
    }

    override fun invoke(value: T) {
        val observers = observers.get()
        observers.forEach { it(value) }
    }
}

class BehaviourSource<T> internal constructor(initialValue: Any? = NoValue) : Source<T>, Sink<T> {
    private val observers: AtomicRef<List<Observer<T>>> = AtomicRef(emptyList())
    private val _value = AtomicRef(initialValue)

    override fun connect(observer: Observer<T>): Cancellable {
        observers.update { it + observer }

        val cancellable = cancellableOf {
            observers.update { it - observer }
            observer.onComplete()
        }
        observer.onSubscribe(cancellable)

        val value = _value.get()
        if (value !== NoValue) {
            observer.invoke(value as T)
        }

        return cancellable
    }

    override fun invoke(value: T) {
        val observers = observers.get()
        _value.update { value }
        observers.forEach { it(value) }
    }

    val value: T? get() =
        _value.get().takeIf { it !== NoValue } as T?

    object NoValue
}

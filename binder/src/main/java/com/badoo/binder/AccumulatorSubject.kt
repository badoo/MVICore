package com.badoo.binder

import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean

interface Drainable {
    fun drain()
}

class AccumulatorSubject<T>(
    private val initialState: T? = null
) : ObservableSource<T>, Consumer<T>, Drainable {

    private val items: MutableList<T> = mutableListOf()
    private val events: PublishSubject<T> = PublishSubject.create()

    private var drained = AtomicBoolean(false)

    init {
        initialState?.also { items.add(it) }
    }

    override fun subscribe(observer: Observer<in T>) {
        events.subscribe(observer)
        if (!drained.get()) {
            items.forEach { observer.onNext(it) }
        } else {
            initialState?.also { observer.onNext(it) }
        }
    }

    override fun accept(value: T?) {
        value?.also {
            if (!drained.get()) {
                items.add(value)
            }
            events.onNext(value)
        }
    }

    override fun drain() {
        if (!drained.get()) {
            drained.set(true)
            items.clear()
        }
    }

    companion object {
        fun <T> create() = AccumulatorSubject<T>()
    }
}

package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Observer
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.cancellableOf

class ValueSource<out T>(vararg values: T) : Source<T> {
    private val valuesToEmit = values

    override fun connect(observer: Observer<T>): Cancellable {
        val cancellable = cancellableOf { }
        observer.onSubscribe(cancellable)

        valuesToEmit.forEach {
            observer.accept(it)
        }

        if (!cancellable.isCancelled) {
            observer.onComplete()
        }

        return cancellable
    }
}


package com.badoo.mvicore.rx

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import io.reactivex.Observer
import io.reactivex.functions.Consumer

fun <T> Consumer<T>.toSink() = object : Sink<T> {
    override fun accept(value: T) {
        this@toSink.accept(value)
    }
}

fun <T> Observer<T>.toCommonObserver() = object : com.badoo.mvicore.common.Observer<T> {
    override fun onComplete() {
        this@toCommonObserver.onComplete()
    }

    override fun onError(e: Throwable) {
        this@toCommonObserver.onError(e)
    }

    override fun accept(value: T) {
        onNext(value)
    }

    override fun onSubscribe(cancellable: Cancellable) {
        onSubscribe(cancellable.toDisposable())
    }

}

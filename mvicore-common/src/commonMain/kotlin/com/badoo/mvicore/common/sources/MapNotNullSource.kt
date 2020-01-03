package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Observer
import com.badoo.mvicore.common.Source

internal class MapNotNullSource<Out, In>(private val delegate: Source<Out>, private val mapper: (Out) -> In?): Source<In> {
    override fun connect(observer: Observer<In>): Cancellable =
        delegate.connect(MapNotNullObserver(observer, mapper))

    private class MapNotNullObserver<Out, In>(
        val delegate: Observer<In>,
        private val mapper: (Out) -> In?
    ): Observer<Out> {
        override fun invoke(value: Out) {
            mapper(value)?.let { delegate(it) }
        }

        override fun onSubscribe(cancellable: Cancellable) {
            delegate.onSubscribe(cancellable)
        }

        override fun onComplete() {
            delegate.onComplete()
        }

        override fun onError(throwable: Throwable) {
            delegate.onError(throwable)
        }
    }
}

package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.cancellableOf
import com.badoo.mvicore.common.cancelled

internal class ValueSource<T>(vararg values: T) : Source<T> {
    private val valuesToEmit = values
    private val cancellable = cancellableOf { }

    override fun connect(sink: Sink<T>): Cancellable {
        if (!cancellable.isCancelled) {
            valuesToEmit.forEach { sink(it) }
        }
        return cancelled()
    }

//    override fun cancel() {
//        cancellable.cancel()
//    }
//
//    override val isCancelled: Boolean
//        get() = cancellable.isCancelled
}


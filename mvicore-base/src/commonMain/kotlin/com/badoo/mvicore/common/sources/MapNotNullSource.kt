package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source

internal class MapNotNullSource<Out, In>(private val delegate: Source<Out>, private val mapper: (Out) -> In?): Source<In> {
    override fun connect(sink: Sink<In>): Cancellable =
        delegate.connect {
            mapper(it)?.let { sink(it) }
        }

    override fun cancel() {
        delegate.cancel()
    }

    override val isCancelled: Boolean
        get() = delegate.isCancelled
}

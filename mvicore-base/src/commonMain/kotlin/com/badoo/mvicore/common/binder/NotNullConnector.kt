package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source

internal class NotNullConnector<Out, In>(private val mapper: (Out) -> In?): Connector<Out, In> {
    override fun invoke(element: Source<out Out>): Source<In> {
        return MapNotNullSource(element, mapper)
    }

    override fun toString(): String =
        mapper.toString()
}

private class MapNotNullSource<Out, In>(private val delegate: Source<Out>, private val mapper: (Out) -> In?): Source<In> {
    override fun connect(sink: Sink<In>): Cancellable =
        delegate.connect {
            mapper(it)?.let { sink(it) }
        }

    override fun cancel() {
        delegate.cancel()
    }
}

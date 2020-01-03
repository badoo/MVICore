package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.sources.MapNotNullSource

internal class NotNullConnector<Out, In>(private val mapper: (Out) -> In?): Connector<Out, In> {
    override fun invoke(source: Source<out Out>): Source<In> {
        return MapNotNullSource(source, mapper)
    }

    override fun toString(): String =
        mapper.toString()
}

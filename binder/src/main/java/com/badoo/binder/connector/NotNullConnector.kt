package com.badoo.binder.connector

import io.reactivex.Observable
import io.reactivex.Observable.wrap
import io.reactivex.ObservableSource

internal class NotNullConnector<Out, In>(private val mapper: (Out) -> In?): Connector<Out, In> {
    override fun invoke(element: ObservableSource<out Out>): ObservableSource<In> =
        wrap(element)
            .flatMap {
                mapper(it)
                    ?.let { Observable.just(it) }
                    ?: Observable.empty()
            }

    override fun toString(): String =
        mapper.toString()
}

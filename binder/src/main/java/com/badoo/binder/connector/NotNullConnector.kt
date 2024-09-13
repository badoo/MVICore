package com.badoo.binder.connector

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.wrap
import io.reactivex.rxjava3.core.ObservableSource

internal class NotNullConnector<Out : Any, In : Any>(private val mapper: (Out) -> In?): Connector<Out, In> {
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

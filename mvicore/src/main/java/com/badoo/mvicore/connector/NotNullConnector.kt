package com.badoo.mvicore.connector

import io.reactivex.Observable

internal class NotNullConnector<Out, In>(private val mapper: (Out) -> In?): Connector<Out, In> {
    override fun invoke(element: Out): Observable<In> =
        mapper(element)
            ?.let { Observable.just(it) }
            ?: Observable.empty()

}
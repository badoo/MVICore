package com.badoo.mvicore.extension

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.functions.Consumer

@Deprecated(
    "Part of internal api, not supposed to be visible outside",
    ReplaceWith(
        "flatMap { mapper(it)?.let { Observable.just(it) } ?: Observable.empty() }",
        "io.reactivex.Observable"
    )
)
inline fun <T, R> Observable<T>.mapNotNull(crossinline mapper: (T) -> R?): Observable<R> =
    flatMap {
        mapper(it)
            ?.let { Observable.just(it) }
            ?: Observable.empty()
    }

fun <T> Observer<T>.asConsumer() = Consumer<T> { onNext(it) }


package com.badoo.mvicore.extension

import io.reactivex.Observable

inline fun <T, R> Observable<T>.mapNotNull(crossinline mapper: (T) -> R?): Observable<R> =
    flatMap {
        mapper(it)
            ?.let { Observable.just(it) }
            ?: Observable.empty()
    }


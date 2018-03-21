package com.badoo.mvicore2.utils

import io.reactivex.Observable

fun <T, R> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R> = flatMap { mapper(it)?.let { Observable.just(it) } ?: Observable.empty() }

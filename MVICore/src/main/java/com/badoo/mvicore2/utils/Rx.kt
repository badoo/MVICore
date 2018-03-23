package com.badoo.mvicore2.utils

import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.ObservableSource

fun <T, R> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R> = flatMap { mapper(it)?.let { Observable.just(it) } ?: Observable.empty() }

fun <T> CompletableSource.andThenEmmit(item: T): ObservableSource<T> = Completable.wrap(this).andThen(Observable.just(item))

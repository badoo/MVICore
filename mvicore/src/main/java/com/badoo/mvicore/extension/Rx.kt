package com.badoo.mvicore.extension

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer

fun <T : Any> Observer<T>.asConsumer() = Consumer<T> { onNext(it) }

internal fun <T : Any> Observable<T>.subscribeOnNullable(scheduler: Scheduler?): Observable<T> =
    if (scheduler != null) subscribeOn(scheduler) else this

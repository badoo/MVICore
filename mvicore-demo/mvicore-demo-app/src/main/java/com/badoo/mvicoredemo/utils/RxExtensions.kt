package com.badoo.mvicoredemo.utils

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

fun <T1 : Any, T2 : Any> combineLatest(
    o1: ObservableSource<T1>,
    o2: ObservableSource<T2>
): ObservableSource<Pair<T1, T2>> =
    Observable.combineLatest(
        o1,
        o2
    ) { t1, t2 -> t1 to t2 }

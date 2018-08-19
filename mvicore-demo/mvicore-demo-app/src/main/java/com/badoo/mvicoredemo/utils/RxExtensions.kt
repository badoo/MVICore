package com.badoo.mvicoredemo.utils

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction

fun <T1, T2> combineLatest(o1: ObservableSource<T1>, o2: ObservableSource<T2>): ObservableSource<Pair<T1, T2>> =
    Observable.combineLatest(
        o1,
        o2,
        BiFunction<T1, T2, Pair<T1, T2>> { t1, t2 ->
            t1 to t2
        }
    )

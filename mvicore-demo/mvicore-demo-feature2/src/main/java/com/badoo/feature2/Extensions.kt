package com.badoo.feature2

import io.reactivex.Observable
import java.util.concurrent.ThreadLocalRandom

fun <T> Observable<T>.randomlyThrowAnException(): Observable<T> =
    doOnNext {
        if (ThreadLocalRandom.current().nextInt(10) == 0) throw RuntimeException("Test exception")
    }


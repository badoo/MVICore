package com.badoo.feature2

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.ThreadLocalRandom

fun <T : Any> Observable<T>.randomlyThrowAnException(): Observable<T> =
    doOnNext {
        if (ThreadLocalRandom.current().nextInt(10) == 0) throw RuntimeException("Test exception")
    }


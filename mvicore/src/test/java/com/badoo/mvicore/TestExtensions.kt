package com.badoo.mvicore

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.onNextEvents() =
        events[0]

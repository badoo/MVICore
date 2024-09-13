package com.badoo.mvicore

import io.reactivex.rxjava3.observers.TestObserver

fun <T> TestObserver<T>.onNextEvents() =
        values()

package com.badoo.mvicore2.binder.internal

import com.badoo.mvicore2.binder.extensions.TestBindable
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject

internal class TestBindableImpl<In : Any, Out:Any>(
        private val input: PublishRelay<In> = PublishRelay.create(),
        override val output: PublishSubject<Out> = PublishSubject.create(),
        override val observer: TestObserver<In> = TestObserver.create()
) : TestBindable<In, Out>,
        ObservableSource<Out> by output,
        Consumer<In> by input {

    init {
        input.subscribe(observer)
    }
}

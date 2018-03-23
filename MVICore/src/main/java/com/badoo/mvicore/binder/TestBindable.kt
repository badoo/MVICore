package com.badoo.mvicore.binder

import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject

class TestBindable<In, Out>(
        val input: PublishSubject<In> = PublishSubject.create(),
        val output: PublishSubject<Out> = PublishSubject.create(),
        val observer: TestObserver<In> = TestObserver.create()
) : Bindable<In, Out>,
        ObservableSource<Out> by output,
        Observer<In> by input {

    init {
        input.subscribe(observer)
    }
}

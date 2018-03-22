package com.badoo.mvicore2.binder

import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject

class TestBindable<In, Out>(
        private val input: PublishSubject<In> = PublishSubject.create(),
        val output: PublishSubject<Out> = PublishSubject.create(),
        val received: TestObserver<In> = TestObserver.create()
) : Bindable<In, Out>,
        ObservableSource<Out> by output,
        Observer<In> by input {

    init {
        input.subscribe(received)
    }
}

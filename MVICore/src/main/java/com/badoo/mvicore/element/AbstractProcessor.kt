package com.badoo.mvicore.element

import io.reactivex.ObservableSource
import io.reactivex.subjects.PublishSubject

abstract class AbstractProcessor<In, Out>(
    private val output: PublishSubject<Out> = PublishSubject.create()
) : Processor<In, Out>, ObservableSource<Out> by output {

    fun publish(out: Out) {
        output.onNext(out)
    }
}

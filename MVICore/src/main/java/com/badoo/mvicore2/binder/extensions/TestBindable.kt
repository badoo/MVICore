package com.badoo.mvicore2.binder.extensions

import com.badoo.mvicore2.binder.Processor
import com.badoo.mvicore2.binder.internal.TestBindableImpl
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject

interface TestBindable<In : Any, Out : Any> : Processor<In, Out> {

    val output : PublishSubject<Out>
    val observer: TestObserver<In>

    companion object {

        fun <In : Any, Out : Any> create(): TestBindable<In, Out> = TestBindableImpl()
    }
}

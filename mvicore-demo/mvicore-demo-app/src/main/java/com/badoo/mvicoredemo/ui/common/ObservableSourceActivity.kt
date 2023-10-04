package com.badoo.mvicoredemo.ui.common

import com.badoo.binder.AccumulatorSubject
import io.reactivex.ObservableSource
import io.reactivex.Observer

abstract class ObservableSourceActivity<T> : DebugActivity(), ObservableSource<T> {

    private val source = AccumulatorSubject.create<T>()

    protected fun onNext(t: T) {
        source.accept(t)
    }

    override fun subscribe(observer: Observer<in T>) {
        source.subscribe(observer)
    }
}

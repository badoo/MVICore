package com.badoo.mvicoredemo.ui.common

import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class ObservableSourceActivity<T : Any> : DebugActivity(), ObservableSource<T> {

    private val source = PublishSubject.create<T>()

    protected fun onNext(t: T) {
        source.onNext(t)
    }

    override fun subscribe(observer: Observer<in T>) {
        source.subscribe(observer)
    }
}

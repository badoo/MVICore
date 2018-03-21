package com.badoo.mvicore2.binder

import io.reactivex.ObservableSource
import io.reactivex.Observer

interface Bindable<In, Out> : ObservableSource<Out>, Observer<In> {

    companion object {

        fun <In, Out> test(): TestBindable<In, Out> = TestBindable()
    }
}

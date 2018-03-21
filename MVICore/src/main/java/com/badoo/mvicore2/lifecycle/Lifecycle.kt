package com.badoo.mvicore2.lifecycle

import com.badoo.mvicore2.lifecycle.internal.FromObservableSource
import io.reactivex.ObservableSource

interface Lifecycle : ObservableSource<Lifecycle.Event> {

    enum class Event {
        CREATE,
        DESTROY
    }

    companion object {

        fun test() : TestLifecycle = TestLifecycle()

        fun wrap(source: ObservableSource<Event>) : Lifecycle = FromObservableSource(source)
    }

}

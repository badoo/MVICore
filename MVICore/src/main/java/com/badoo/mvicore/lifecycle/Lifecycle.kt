package com.badoo.mvicore.lifecycle

import com.badoo.mvicore.lifecycle.internal.FromObservableSource
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

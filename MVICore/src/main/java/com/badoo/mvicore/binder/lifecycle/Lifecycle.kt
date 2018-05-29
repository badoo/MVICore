package com.badoo.mvicore.binder.lifecycle

import com.badoo.mvicore.binder.lifecycle.internal.FromObservableSource
import io.reactivex.Observable
import io.reactivex.ObservableSource

interface Lifecycle : ObservableSource<Lifecycle.Event> {

    enum class Event {
        BEGIN,
        END
    }

    companion object {

        fun indeterminate(): Lifecycle =
            Indeterminate

        fun manual(): ManualLifecycle =
            ManualLifecycle()

        fun wrap(source: ObservableSource<Event>) : Lifecycle =
            FromObservableSource(source)

        internal object Indeterminate : Lifecycle, ObservableSource<Lifecycle.Event> by Observable.never()
    }

}

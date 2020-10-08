package com.badoo.binder.lifecycle

import com.badoo.binder.lifecycle.internal.FromObservableSource
import io.reactivex.rxjava3.core.ObservableSource

interface Lifecycle : ObservableSource<Lifecycle.Event> {

    enum class Event {
        BEGIN,
        END
    }

    companion object {

        fun manual(): ManualLifecycle =
            ManualLifecycle()

        fun wrap(source: ObservableSource<Event>) : Lifecycle =
            FromObservableSource(source)
    }

}

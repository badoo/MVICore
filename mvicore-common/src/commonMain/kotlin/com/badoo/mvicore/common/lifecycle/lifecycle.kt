package com.badoo.mvicore.common.lifecycle

import com.badoo.mvicore.common.BehaviourSource
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.END

interface Lifecycle : Source<Lifecycle.Event> {
    enum class Event {
        BEGIN, END
    }

    companion object {
        fun manual() = ManualLifecycle()
    }
}

class ManualLifecycle(
    private val source: BehaviourSource<Lifecycle.Event> = BehaviourSource()
) : Lifecycle, Source<Lifecycle.Event> by source {
    fun begin() {
        source.accept(BEGIN)
    }

    fun end() {
        source.accept(END)
    }
}

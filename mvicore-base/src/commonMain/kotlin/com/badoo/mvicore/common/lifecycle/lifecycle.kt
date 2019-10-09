package com.badoo.mvicore.common.lifecycle

import com.badoo.mvicore.common.SimpleSource
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
    private val source: SimpleSource<Lifecycle.Event> = SimpleSource(initialValue = null, emitOnConnect = true)
) : Lifecycle, Source<Lifecycle.Event> by source {
    fun begin() {
        source(BEGIN)
    }

    fun end() {
        source(END)
    }
}

package com.badoo.mvicore.common.lifecycle

import com.badoo.mvicore.common.SimpleSource
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.END
import com.badoo.mvicore.common.source

interface Lifecycle : Source<Lifecycle.Event> {
    enum class Event {
        BEGIN, END
    }

    fun manual() = ManualLifecycle()
}

class ManualLifecycle(private val source: SimpleSource<Lifecycle.Event> = source()) : Lifecycle, Source<Lifecycle.Event> by source {
    fun begin() {
        source(BEGIN)
    }

    fun end() {
        source(END)
    }
}

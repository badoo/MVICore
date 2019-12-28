package com.badoo.mvicore.common.lifecycle

import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.SourceImpl
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
    private val source: SourceImpl<Lifecycle.Event> = SourceImpl(initialValue = null, emitOnConnect = true)
) : Lifecycle, Source<Lifecycle.Event> by source {
    fun begin() {
        source(BEGIN)
    }

    fun end() {
        source(END)
    }
}

package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.cancellableOf

internal class DelayUntilSource<T>(
    private val signal: Source<Boolean>,
    private val delegate: Source<T>
): Source<T> {
    override fun connect(sink: Sink<T>): Cancellable {
        val delayedSink = DelayedSink(sink)
        val cancelDelay = signal.connect { if (it) { delayedSink.send() } }
        val cancelSubscription = delegate.connect(delayedSink)
        return cancellableOf {
            cancelDelay.cancel()
            cancelSubscription.cancel()
        }
    }

    override fun cancel() {
        delegate.cancel()
    }

    override val isCancelled: Boolean
        get() = delegate.isCancelled

    private class DelayedSink<T>(private val delegate: Sink<T>) : Sink<T> {
        private var passThrough = false
        private val events = mutableListOf<T>()

        override fun invoke(value: T) {
            if (passThrough) {
                delegate(value)
            } else {
                events += value
            }
        }

        fun send() {
            passThrough = true
            events.forEach { delegate(it) }
            events.clear()
        }
    }
}

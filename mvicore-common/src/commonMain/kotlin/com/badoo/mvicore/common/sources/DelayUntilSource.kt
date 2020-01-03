package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.AtomicRef
import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.CompositeCancellable
import com.badoo.mvicore.common.Observer
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.update

internal class DelayUntilSource<T>(
    private val signal: Source<Boolean>,
    private val delegate: Source<T>
): Source<T> {
    override fun connect(observer: Observer<T>): Cancellable =
        delegate.connect(DelayedObserver(observer, signal))

    private class DelayedObserver<T>(
        private val delegate: Observer<T>,
        signal: Source<Boolean>
    ) : Observer<T> {
        private val passThrough = AtomicRef(false)
        private val isCompleted = AtomicRef(false)
        private val events: AtomicRef<List<T>> = AtomicRef(emptyList())
        private val cancellable = signal.connect { if (it) send() }

        override fun invoke(value: T) {
            if (passThrough.get()) {
                delegate(value)
            } else {
                events.update { it + value }
            }
        }

        private fun send() {
            passThrough.compareAndSet(false, true)

            val events = this.events.get()
            events.forEach { delegate(it) }
            this.events.update { emptyList() }

            completeDownstream()
        }

        override fun onSubscribe(cancellable: Cancellable) {
            delegate.onSubscribe(CompositeCancellable(cancellable, this.cancellable))
        }

        override fun onComplete() {
            isCompleted.compareAndSet(false, true)
            completeDownstream()
        }

        private fun completeDownstream() {
            if (isCompleted.get() && (passThrough.get() || cancellable.isCancelled)) {
                delegate.onComplete()
            }
        }

        override fun onError(throwable: Throwable) {
            delegate.onError(throwable)
        }
    }
}

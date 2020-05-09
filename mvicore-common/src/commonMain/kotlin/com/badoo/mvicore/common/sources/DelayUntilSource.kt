package com.badoo.mvicore.common.sources

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.CompositeCancellable
import com.badoo.mvicore.common.Observer
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.connect
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.update

internal class DelayUntilSource<out T>(
    private val signal: Source<Boolean>,
    private val delegate: Source<T>
): Source<T> {
    override fun connect(observer: Observer<T>): Cancellable =
        delegate.connect(DelayedObserver(observer, signal))

    private class DelayedObserver<T>(
        private val delegate: Observer<T>,
        signal: Source<Boolean>
    ) : Observer<T> {
        private val passThrough = AtomicBoolean(false)
        private val isCompleted = AtomicBoolean(false)
        private val events: AtomicReference<List<T>> = AtomicReference(emptyList())
        private val cancellable = signal.connect { if (it) send() }

        override fun accept(value: T) {
            if (passThrough.value) {
                delegate.accept(value)
            } else {
                events.update { it + value }
            }
        }

        private fun send() {
            passThrough.compareAndSet(false, true)

            val events = this.events.value
            events.forEach { delegate.accept(it) }
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
            if (isCompleted.value && (passThrough.value || cancellable.isCancelled)) {
                delegate.onComplete()
            }
        }

        override fun onError(throwable: Throwable) {
            delegate.onError(throwable)
        }
    }
}

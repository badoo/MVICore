package com.badoo.mvicore.common

import kotlin.test.Test
import kotlin.test.assertEquals

class CancellableTest {
    @Test
    fun cancellable_is_not_cancelled_by_default() {
        val cancellable = cancellableOf {  }

        assertEquals(cancellable.isCancelled, false)
    }

    @Test
    fun cancellable_action_is_executed_on_cancel() {
        val sink = TestSink<Unit>()
        val cancellable = cancellableOf { sink(Unit) }

        cancellable.cancel()

        sink.assertValues(Unit)
    }

    @Test
    fun cancellable_action_is_executed_only_once_on_cancel() {
        val sink = TestSink<Unit>()
        val cancellable = cancellableOf { sink(Unit) }

        cancellable.cancel()
        cancellable.cancel()

        sink.assertValues(Unit)
    }

    @Test
    fun cancellable_is_cancelled_on_cancel() {
        val cancellable = cancellableOf { }

        cancellable.cancel()

        assertEquals(cancellable.isCancelled, true)
    }

    @Test
    fun composite_cancellable_cancels_children() {
        val childCancellable = cancellableOf {  }
        val compositeCancellable = CompositeCancellable(childCancellable)

        compositeCancellable.cancel()

        assertEquals(childCancellable.isCancelled, true)
    }

    @Test
    fun composite_cancellable_does_not_cancel_removed_children() {
        val childCancellable = cancellableOf {  }
        val compositeCancellable = CompositeCancellable(childCancellable)

        compositeCancellable -= childCancellable
        compositeCancellable.cancel()

        assertEquals(childCancellable.isCancelled, false)
    }

    @Test
    fun composite_cancellable_cancel_added_children() {
        val childCancellable = cancellableOf {  }
        val compositeCancellable = CompositeCancellable()

        compositeCancellable += childCancellable
        compositeCancellable.cancel()

        assertEquals(childCancellable.isCancelled, true)
    }
}

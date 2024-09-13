package com.badoo.binder

import com.badoo.binder.lifecycle.Lifecycle
import com.badoo.binder.lifecycle.ManualLifecycle
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class LifecycleTest {

    private val lifecycle: ManualLifecycle = Lifecycle.manual()
    private val source = PublishSubject.create<Int>()
    private val consumer = TestConsumer<Int>()

    @Test
    fun `connection starts when lifecycle begins`() {
        createBinding()

        source.onNext(0)
        lifecycle.begin()
        source.onNext(1)

        consumer.assertValues(1)
    }

    @Test
    fun `connection starts if lifecycle has already begun`() {
        lifecycle.begin()

        createBinding()

        source.onNext(0)
        source.onNext(1)

        consumer.assertValues(0, 1)
    }

    @Test
    fun `connection interrupts when lifecycle stops`() {
        lifecycle.begin()
        createBinding()

        source.onNext(0)

        lifecycle.end()
        source.onNext(1)

        consumer.assertValues(0)
    }

    @Test
    fun `connection restarts when lifecycle restarts`() {
        lifecycle.begin()
        createBinding()

        source.onNext(0)
        lifecycle.end()
        source.onNext(1)
        lifecycle.begin()
        source.onNext(2)

        consumer.assertValues(0, 2)
    }

    @Test
    fun `binder does not resubscribe on consecutive begin events`() {
        val mockedSource: ObservableSource<Int> = mock()
        createBinding(from = mockedSource)

        lifecycle.begin()
        lifecycle.begin()

        verify(mockedSource, times(1)).subscribe(any())
    }

    private fun createBinding(from: ObservableSource<Int> = source, to: Consumer<Int> = consumer) {
        val binder = Binder(lifecycle)
        binder.bind(from to to)
    }
}

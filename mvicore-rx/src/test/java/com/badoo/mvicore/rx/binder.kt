package com.badoo.mvicore.rx

import com.badoo.mvicore.common.binder.bind
import com.badoo.mvicore.common.binder.binder
import com.badoo.mvicore.common.lifecycle.Lifecycle
import com.badoo.mvicore.rx.binder.Connector
import com.badoo.mvicore.rx.binder.bind
import com.badoo.mvicore.rx.binder.using
import io.reactivex.Observable
import io.reactivex.Observable.wrap
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.junit.Test

class BinderTest {
    private val source = PublishSubject.create<Int>()
    private val sink = TestConsumer<Int>()

    @Test
    fun binder_without_lifecycle_connects_source_and_sink() {
        binder().apply {
            bind(source to sink)
        }

        source.onNext(0)
        sink.assertValues(0)
    }

    @Test
    fun binder_without_lifecycle_does_not_connect_source_and_sink_after_cancel() {
        val binder = binder().apply {
            bind(source to sink)
        }

        binder.cancel()

        source.onNext(0)
        sink.assertNoValues()
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_using_mapper() {
        binder().apply {
            bind(source to sink using { it + 1 })
        }

        source.onNext(0)
        sink.assertValues(1)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_skips_nulls_from_mapper() {
        binder().apply {
            bind(source to sink using { if (it % 2 == 0) null else it })
        }

        source.onNext(0)
        source.onNext(1)
        source.onNext(2)
        sink.assertValues(1)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_using_connector() {
        val connector = object : Connector<Int, Int> {
            override fun invoke(source: ObservableSource<out Int>): ObservableSource<out Int> =
                wrap(source).flatMap { Observable.just(it, it + 1) }
        }

        binder().apply {
            bind(source to sink using connector)
        }

        source.onNext(0)
        sink.assertValues(0, 1)
    }

    @Test
    fun binder_with_lifecycle_connects_source_and_sink_when_active() {
        val lifecycle = Lifecycle.manual()
        binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.onNext(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_does_not_connect_source_and_sink_before_active() {
        val lifecycle = Lifecycle.manual()
        binder(lifecycle).apply {
            bind(source to sink)
        }

        source.onNext(0)
        lifecycle.begin()

        sink.assertNoValues()
    }

    @Test
    fun binder_with_lifecycle_disconnect_source_and_sink_after_end() {
        val lifecycle = Lifecycle.manual()
        binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.onNext(0)
        lifecycle.end()
        source.onNext(1)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_reconnect_source_and_sink_after_begin() {
        val lifecycle = Lifecycle.manual()
        binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.onNext(0)
        lifecycle.end()
        source.onNext(1)
        lifecycle.begin()
        source.onNext(2)

        sink.assertValues(0, 2)
    }

    @Test
    fun binder_with_lifecycle_does_not_reconnect_source_and_sink_after_cancel() {
        val lifecycle = Lifecycle.manual()
        val binder = binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.onNext(0)
        lifecycle.end()
        binder.cancel()
        lifecycle.begin()
        source.onNext(2)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_connects_source_and_sink_if_lifecycle_started() {
        val lifecycle = Lifecycle.manual()
        lifecycle.begin()

        binder(lifecycle).apply {
            bind(source to sink)
        }

        source.onNext(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_does_not_reconnect_on_duplicated_lifecycle_events() {
        val lifecycle = Lifecycle.manual()

        binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        lifecycle.begin()

        source.onNext(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_covariant_endpoints_compile_for_pair() {
        val sink = Consumer<Any> { /* no-op */ }
        binder().bind(source to sink)
    }

    @Test
    fun binder_covariant_endpoints_compile_for_connection() {
        val sink = Consumer { _: Any -> /* no-op */ }
        val intToString: (Int) -> String = { it.toString() }
        binder().bind(source to sink using intToString)
    }

    @Test
    fun binder_delivers_message_to_all_sinks_on_dispose() {
        val binder = binder {
            val sink2 = Consumer { _: Int -> this.cancel() }

            bind(source to sink2)
            bind(source to sink)
        }

        source.onNext(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_messages_sent_on_initialize_are_not_lost() {
        val passThroughSource = PublishSubject.create<Int>()
        binder {
            bind(source to Consumer { passThroughSource.onNext(it) })
            source.onNext(0)
            bind(passThroughSource to sink)
        }

        sink.assertValues(0)
    }
}

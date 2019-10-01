package com.badoo.mvicore.common

import com.badoo.mvicore.common.binder.Binder
import com.badoo.mvicore.common.binder.NotNullConnector
import com.badoo.mvicore.common.binder.using
import kotlin.test.Test
import kotlin.test.assertEquals

class BinderTest {
    private val source = source<Int>()
    private val sink = TestSink<Int>()

    @Test
    fun binder_without_lifecycle_connects_source_and_sink() {
        Binder().apply {
            bind(source to sink)
        }

        source.invoke(0)
        assertEquals(listOf(0), sink.values)
    }

    @Test
    fun binder_without_lifecycle_does_not_connect_source_and_sink_after_cancel() {
        val binder = Binder().apply {
            bind(source to sink)
        }

        binder.cancel()

        source.invoke(0)
        assertEquals(emptyList(), sink.values)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_using_mapper() {
        Binder().apply {
            bind(source to sink using { it + 1 })
        }

        source.invoke(0)
        assertEquals(listOf(1), sink.values)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_skips_nulls_from_mapper() {
        Binder().apply {
            bind(source to sink using { if (it % 2 == 0) null else it })
        }

        source.invoke(0)
        source.invoke(1)
        source.invoke(2)
        assertEquals(listOf(1), sink.values)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_using_connector() {
        val connector = NotNullConnector<Int, Int> { it }
        Binder().apply {
            bind(source to sink using connector)
        }

        source.invoke(0)
        assertEquals(listOf(0), sink.values)
    }
}

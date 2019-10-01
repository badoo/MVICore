package com.badoo.mvicore.common

import kotlin.test.Test
import kotlin.test.assertEquals

class SourceTest {

    @Test
    fun source_connected_sink_receives_no_values() {
        val source = source<Int>()
        val sink = TestSink<Int>()

        source.connect(sink)
        assertEquals(emptyList(), sink.values)
    }

    @Test
    fun source_with_initial_value_connected_sink_receives_a_value() {
        val source = source(0)
        val sink = TestSink<Int>()

        source.connect(sink)
        assertEquals(listOf(0), sink.values)
    }

    @Test
    fun source_sends_value_after_connect_connected_sink_receives_a_value() {
        val source = source<Int>()
        val sink = TestSink<Int>()

        source.connect(sink)
        source.invoke(0)

        assertEquals(listOf(0), sink.values)
    }

    @Test
    fun source_sends_value_after_disconnect_connected_sink_receives_no_values() {
        val source = source<Int>()
        val sink = TestSink<Int>()
        val cancellable = source.connect(sink)
        cancellable.cancel()

        source.invoke(0)

        assertEquals(emptyList(), sink.values)
    }
}

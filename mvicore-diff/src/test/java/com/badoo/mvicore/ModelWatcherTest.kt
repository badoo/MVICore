package com.badoo.mvicore

import com.badoo.mvicore.util.Model
import com.badoo.mvicore.util.testWatcher
import org.junit.Test
import kotlin.test.assertEquals

class ModelWatcherTest {

    @Test
    fun `invokes callback when field changes`() {
        val results = testWatcher<Int>(
            listOf(
                Model(int = 0),
                Model(int = 1)
            )
        ) { updates ->
            watch(Model::int) {
                updates += it
            }
        }

        assertEquals(2, results.size)
    }

    @Test
    fun `does not invoke callback when field does not change`() {
        val results = testWatcher<Int>(
            listOf(
                Model(int = 0),
                Model(int = 0)
            )
        ) { updates ->
            watch(Model::int) {
                updates += it
            }
        }

        assertEquals(1, results.size)
    }

    @Test
    fun `emits nullable fields on start`() {
        val results = testWatcher<Boolean?>(
            listOf(
                Model(nullable = null)
            )
        ) { updates ->
            watch(Model::nullable) {
                updates += it
            }
        }

        assertEquals(1, results.size)
    }

    @Test
    fun `by default compares by value`() {
        val results = testWatcher<List<String>>(
            listOf(
                Model(list = listOf("")),
                Model(list = listOf(""))
            )
        ) { updates ->
            watch(Model::list) {
                updates += it
            }
        }

        assertEquals(1, results.size)
    }
}

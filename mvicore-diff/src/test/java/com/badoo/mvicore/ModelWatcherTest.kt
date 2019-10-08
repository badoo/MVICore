package com.badoo.mvicore

import com.badoo.mvicore.util.Model
import com.badoo.mvicore.util.testWatcher
import org.junit.Test
import kotlin.test.assertEquals

class ModelWatcherTest {

    @Test
    fun `invokes callback when field changes`() {
        val results = testWatcher<Int, Model>(
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
        val results = testWatcher<Int, Model>(
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
        val results = testWatcher<Boolean?, Model>(
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
        val results = testWatcher<List<String>, Model>(
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

    @Test
    fun `invokes callback using dsl`() {
        val results = testWatcher<Int, Model>(
            listOf(
                Model(int = 0), Model(int = 1)
            )
        ) { updates ->
            Model::int {
                updates += it
            }
        }

        assertEquals(2, results.size)
    }

    @Test
    fun `invokes callback using dsl with diffStrategy`() {
        val results = testWatcher<List<String>, Model>(
            listOf(
                Model(list = listOf("")),
                Model(list = listOf(""))
            )
        ) { updates ->
            val byRef = byRef<List<String>>()
            Model::list using byRef {
                updates += it
            }
        }

        assertEquals(2, results.size)
    }

    @Test
    fun `invokes callback with combined diffStrategy using "or"`() {
        val results = testWatcher<Model, Model>(
            listOf(
                Model(list = listOf(""), int = 1),
                Model(list = listOf(""), int = 1, nullable = false),
                Model(list = listOf(""), int = 2)
            )
        ) { updates ->
            (Model::list or Model::int) {
                updates += it
            }
        }

        assertEquals(2, results.size)
    }

    @Test
    fun `invokes callback with combined diffStrategy using "and"`() {
        val results = testWatcher<Model, Model>(
            listOf(
                Model(list = listOf(""), int = 1),
                Model(int = 1),
                Model(list = listOf(""), int = 2)
            )
        ) { updates ->
            (Model::list and Model::int) {
                updates += it
            }
        }

        assertEquals(2, results.size)
    }

    @Test
    fun `invokes callback after clear`() {
        val results = mutableListOf<List<String>>()
        val watcher = modelWatcher<Model> {
            Model::list {
                results += it
            }
        }

        watcher.invoke(Model(list = listOf("")))
        watcher.clear()
        watcher.invoke(Model(list = listOf("")))

        assertEquals(2, results.size)
    }
}

package com.badoo.mvicore

import com.badoo.mvicore.util.Model
import com.badoo.mvicore.util.testWatcher
import org.junit.Test
import kotlin.test.assertEquals

class HelperTest {
    @Test
    fun `by value strategy compares by value`() {
        val results = testWatcher<List<String>, Model>(
            listOf(
                Model(list = listOf("")),
                Model(list = listOf(""))
            )
        ) { updates ->
            watch(Model::list, byValue()) {
                updates += it
            }
        }

        assertEquals(1, results.size)
    }

    @Test
    fun `by ref strategy compares by reference`() {
        val results = testWatcher<List<String>, Model>(
            listOf(
                Model(list = listOf("")),
                Model(list = listOf(""))
            )
        ) { updates ->
            watch(Model::list, byRef()) {
                updates += it
            }
        }

        assertEquals(2, results.size)
    }
}

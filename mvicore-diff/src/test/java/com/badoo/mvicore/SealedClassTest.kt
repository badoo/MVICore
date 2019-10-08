package com.badoo.mvicore

import com.badoo.mvicore.util.Nested
import com.badoo.mvicore.util.SealedModel
import com.badoo.mvicore.util.testWatcher
import org.junit.Test
import kotlin.test.assertEquals

class SealedClassTest {
    @Test
    fun `sealed class subtypes are triggered every time type has changed`() {
        val results = testWatcher<List<String>, SealedModel>(
            listOf(
                SealedModel.Value(list = listOf("")),
                SealedModel.Nothing,
                SealedModel.Value(list = listOf(""))
            )
        ) { updates ->
            type<SealedModel.Value> {
                SealedModel.Value::list {
                    updates += it
                }
            }

            objectType<SealedModel.Nothing> {
                updates += emptyList<String>()
            }
        }

        assertEquals(3, results.size)
    }

    @Test
    fun `sealed class subtypes work with common properties`() {
        val results = testWatcher<List<String>, SealedModel>(
            listOf(
                SealedModel.Value(),
                SealedModel.Nothing,
                SealedModel.Value()
            )
        ) { updates ->
            SealedModel::list {
                updates += it
            }
        }

        assertEquals(1, results.size)
    }

    @Test
    fun `sealed class subtypes are cleared independently from common properties`() {
        val results = testWatcher<List<String>, SealedModel>(
            listOf(
                SealedModel.Value(),
                SealedModel.Nothing,
                SealedModel.Value()
            )
        ) { updates ->
            type<SealedModel.Value> {
                SealedModel.Value::list {
                    updates += it
                }
            }
            objectType<SealedModel.Nothing> {
                updates += emptyList<String>()
            }

            SealedModel::list {
                updates += it
            }
        }

        assertEquals(4, results.size)
    }

    @Test
    fun `deeply sealed class subtypes are triggered every time type has changed`() {
        val results = testWatcher<List<String>, Nested>(
            listOf(
                Nested.SubNested.Value(emptyList()),
                Nested.SubNested.Value(emptyList()),
                Nested.Something,
                Nested.SubNested.Nothing,
                Nested.SubNested.Value(emptyList())
            )
        ) { updates ->
            type<Nested.SubNested> {
                type<Nested.SubNested.Value> {
                    Nested.SubNested.Value::list {
                        updates += it
                    }
                }
            }

            objectType<Nested.Something> {
                updates += emptyList<String>()
            }
        }

        assertEquals(3, results.size)
    }
}

package com.badoo.mvicore.todo.feature

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.todo.feature.TodoListFeature.Wish
import com.badoo.mvicore.todo.model.TodoItem
import org.junit.Assert.assertEquals
import org.junit.Test

class TodoListFeatureTest {

    @Test
    fun `creating a todo increments id`() {
        val feature = createFeature()

        feature.accept(
            Wish.Create(TodoItem(title = "Test"))
        )

        assertEquals(
            1,
            feature.state.nextId
        )
    }

    @Test
    fun `creating a todo adds it to the state with incremented id`() {
        val feature = createFeature()

        feature.accept(
            Wish.Create(TodoItem(title = "Test"))
        )
        feature.accept(
            Wish.Create(TodoItem(title = "Test 1"))
        )

        assertEquals(
            listOf(TodoItem(id = 0, title = "Test"), TodoItem(id = 1, title = "Test 1")),
            feature.state.todos
        )
    }

    @Test
    fun `updating a todo replaces it in the state with given id`() {
        val feature = createFeature()
        val initialItems = listOf(TodoItem(title = "Test"), TodoItem(title = "Test 1"))
        val newItem = TodoItem(id = 0, title = "Test", done = true)

        initialItems.forEach {
            feature.accept(Wish.Create(it))
        }
        feature.accept(Wish.Update(newItem))

        assertEquals(
            listOf(newItem, TodoItem(id = 1, title = "Test 1")),
            feature.state.todos
        )
    }

    @Test
    fun `updating todo with not existing id does not change the list`() {
        val feature = createFeature()

        feature.accept(Wish.Create(TodoItem(title = "Test")))
        feature.accept(Wish.Update(TodoItem(id = 1, title = "Test")))

        assertEquals(
            listOf(TodoItem(id = 0, title = "Test")),
            feature.state.todos
        )
    }

    @Test
    fun `deleting todo removes it from a list`() {
        val feature = createFeature()

        feature.accept(
            Wish.Create(TodoItem(title = "Test"))
        )
        feature.accept(
            Wish.Create(TodoItem(title = "Test 1"))
        )

        feature.accept(
            Wish.Delete(TodoItem(id = 1, title = "Test 1"))
        )

        assertEquals(
            listOf(TodoItem(id = 0, title = "Test")),
            feature.state.todos
        )
    }

    private fun createFeature() = TodoListFeature(AndroidTimeCapsule(null))
}

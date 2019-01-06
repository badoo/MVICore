package com.badoo.mvicore.todo.mapper

import com.badoo.mvicore.todo.feature.TodoListFeature
import com.badoo.mvicore.todo.feature.TodoListFeature.Wish.*
import com.badoo.mvicore.todo.model.TodoItem
import com.badoo.mvicore.todo.ui.TodoListView.TodoEvent

object UiEventToWish: (TodoEvent) -> TodoListFeature.Wish? {
    override fun invoke(event: TodoEvent): TodoListFeature.Wish? = when (event) {
        is TodoEvent.UpdateDone -> UpdateDone(event.item)
        is TodoEvent.Create -> Create(TodoItem(title = event.title))
        is TodoEvent.Delete -> Delete(event.item)
    }
}

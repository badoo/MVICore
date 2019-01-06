package com.badoo.mvicore.todo.mapper

import com.badoo.mvicore.todo.feature.TodoListFeature
import com.badoo.mvicore.todo.ui.TodoListView.TodoViewModel

object StateToViewModel: (TodoListFeature.State) -> TodoViewModel {
    override fun invoke(state: TodoListFeature.State): TodoViewModel =
        TodoViewModel(state.todos)
}

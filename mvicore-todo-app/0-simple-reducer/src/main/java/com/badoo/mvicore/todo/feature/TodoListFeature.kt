package com.badoo.mvicore.todo.feature

import android.os.Bundle
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ReducerFeature
import com.badoo.mvicore.todo.feature.TodoListFeature.*
import com.badoo.mvicore.todo.model.TodoItem
import java.io.Serializable

class TodoListFeature(
    timeCapsule: AndroidTimeCapsule
): ReducerFeature<Wish, State, Nothing>(
    initialState = timeCapsule.state() ?: State(),
    reducer = ReducerImpl
) {

    init {
        timeCapsule.register(CAPSULE_KEY) { state.toParcelable() }
    }

    sealed class Wish {
        data class Create(val item: TodoItem) : Wish()
        data class Delete(val item: TodoItem) : Wish()
        data class UpdateDone(val item: TodoItem) : Wish()
    }

    data class State(
        val nextId: Long = 0,
        val todos: List<TodoItem> = emptyList()
    ) : Serializable

    object ReducerImpl : Reducer<State, Wish> {
        override fun invoke(state: State, wish: Wish): State = when (wish) {
            is Wish.Create -> state.copy(
                todos = state.todos + wish.item.copy(id = state.nextId),
                nextId = state.nextId + 1
            )
            is Wish.Delete -> state.copy(
                todos = state.todos - wish.item
            )
            is Wish.UpdateDone -> state.copy(
                todos = state.todos.map {
                    if (it.id == wish.item.id) it.copy(done = !it.done) else it
                }
            )
        }
    }

    companion object {
        private const val CAPSULE_KEY = "TodoFeature"
        private const val STATE_KEY = "TodoFeature.State"

        private fun AndroidTimeCapsule.state() =
            get<Bundle>(CAPSULE_KEY)?.getSerializable(STATE_KEY) as? State

        private fun State.toParcelable() = Bundle().apply { putSerializable(STATE_KEY, this@toParcelable) }
    }
}

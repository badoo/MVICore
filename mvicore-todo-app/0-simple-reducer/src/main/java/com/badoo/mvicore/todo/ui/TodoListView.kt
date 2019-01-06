package com.badoo.mvicore.todo.ui

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.badoo.mvicore.todo.R
import com.badoo.mvicore.todo.model.TodoItem
import com.badoo.mvicore.todo.ui.TodoListView.TodoEvent
import com.badoo.mvicore.todo.ui.TodoListView.TodoViewModel
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class TodoListView(
    root: ViewGroup,
    private val events: PublishSubject<TodoEvent> = PublishSubject.create()
): ObservableSource<TodoEvent> by events, Consumer<TodoViewModel> {

    private val list: RecyclerView = root.findViewById(R.id.main_todoList)
    private val input: EditText = root.findViewById(R.id.main_todoInput)
    private val submit: Button = root.findViewById(R.id.main_todoSubmit)
    private val adapter = TodoListAdapter(events)

    init {
        list.layoutManager = LinearLayoutManager(root.context)
        list.adapter = adapter
        submit.setOnClickListener {
            if (input.text.isNotEmpty()) {
                events.onNext(
                    TodoEvent.Create(input.text.toString())
                )
                input.text.clear()
            }
        }
    }

    override fun accept(model: TodoViewModel) {
        adapter.items = model.todos.sortedWith(TodoComparator)
    }

    object TodoComparator : Comparator<TodoItem> {
        override fun compare(todo1: TodoItem, todo2: TodoItem): Int {
            val doneCompareResult = todo1.done.compareTo(todo2.done)
            return if (doneCompareResult == 0) {
                todo1.id.compareTo(todo2.id)
            } else {
                doneCompareResult
            }
        }
    }

    data class TodoViewModel(
        val todos: List<TodoItem>
    )

    sealed class TodoEvent {
        data class UpdateDone(val item: TodoItem): TodoEvent()
        data class Delete(val item: TodoItem): TodoEvent()
        data class Create(val title: String): TodoEvent()
    }
}

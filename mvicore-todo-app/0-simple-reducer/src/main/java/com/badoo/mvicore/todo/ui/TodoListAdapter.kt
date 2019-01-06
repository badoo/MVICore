package com.badoo.mvicore.todo.ui

import android.graphics.Color
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.badoo.mvicore.todo.R
import com.badoo.mvicore.todo.model.TodoItem
import com.badoo.mvicore.todo.ui.TodoListView.TodoEvent
import com.badoo.mvicore.todo.ui.TodoListView.TodoEvent.Delete
import com.badoo.mvicore.todo.ui.TodoListView.TodoEvent.UpdateDone
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

class TodoListAdapter(
    private val events: PublishSubject<TodoEvent>
): ListAdapter<TodoItem, TodoListAdapter.ViewHolder>(DiffCallback()) {

    var items: List<TodoItem> = emptyList()
        set(value) {
            field = value
            submitList(items)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.accept(items[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), Consumer<TodoItem> {
        private val checkBox: CheckBox = itemView.findViewById(R.id.todoItem_checkbox)
        private val delete: View = itemView.findViewById(R.id.todoItem_delete)
        private var item: TodoItem? = null

        init {
            checkBox.setOnClickListener {
                item?.let {
                    events.onNext(UpdateDone(it))
                }
            }

            delete.setOnClickListener {
                item?.let {
                    events.onNext(Delete(it))
                }
            }
        }

        override fun accept(item: TodoItem) {
            checkBox.text = item.title
            checkBox.isChecked = item.done
            checkBox.setTextColor(
                if (item.done) Color.GRAY else Color.BLACK
            )
            checkBox.paintFlags = checkBox.paintFlags.run {
                if (item.done) {
                    this or STRIKE_THRU_TEXT_FLAG
                } else {
                    this and STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

            this.item = item
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean =
            oldItem == newItem
    }
}

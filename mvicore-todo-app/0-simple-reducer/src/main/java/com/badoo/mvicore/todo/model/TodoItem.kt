package com.badoo.mvicore.todo.model

data class TodoItem(
    val id: Long = 0,
    val title: String,
    val done: Boolean = false
)

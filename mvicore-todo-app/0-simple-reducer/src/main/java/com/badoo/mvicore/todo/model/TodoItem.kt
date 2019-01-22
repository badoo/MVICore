package com.badoo.mvicore.todo.model

import java.io.Serializable

data class TodoItem(
    val id: Long = 0,
    val title: String,
    val done: Boolean = false
): Serializable

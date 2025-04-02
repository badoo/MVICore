package com.badoo.mvicore.util

import com.badoo.mvicore.ModelWatcher
import com.badoo.mvicore.modelWatcher

fun <T, Model : Any> testWatcher(
    models: List<Model>,
    init: ModelWatcher.Builder<Model>.(result: MutableList<T>) -> Unit
): List<T> {
    val updates = mutableListOf<T>()
    val watcher = modelWatcher { init(updates) }
    models.forEach { watcher(it) }
    return updates
}

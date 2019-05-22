package com.badoo.mvicore

class ModelWatcher<T>(
    initBindings: ModelWatcher<T>.Builder.() -> Unit
): (T) -> Unit {

    private class Watcher<T, R>(
        val accessor: T.() -> R,
        val callback: (R) -> Unit,
        val diffStrategy: (R?, R) -> Boolean
    )

    init {
        Builder().apply {
            initBindings()
        }
    }

    private val watchers = mutableListOf<Watcher<T, Any?>>()
    private var state: T? = null

    override fun invoke(value: T) {
        watchers.forEach { element ->
            val old = state?.let { element.accessor(it) }
            val new = element.accessor(value)
            if (state == null || element.diffStrategy(old, new)) {
                element.callback(new)
            }
        }

        state = value
    }

    inner class Builder internal constructor() {
        fun <R> watch(
            accessor: T.() -> R,
            diffStrategy: DiffStrategy<R> = ByValue(),
            callback: (R) -> Unit
        ) {
            watchers += Watcher(
                accessor,
                callback,
                diffStrategy
            ) as Watcher<T, Any?>
        }
    }
}

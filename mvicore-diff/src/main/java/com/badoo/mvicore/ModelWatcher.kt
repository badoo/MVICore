package com.badoo.mvicore

class ModelWatcher<Model : Any> private constructor(
    private val watchers: List<Watcher<Model, Any?>>,
    private val childWatchers: HashMap<Class<Any>, ModelWatcher<Any>>
) {
    private var model: Model? = null

    operator fun invoke(newModel: Model) {
        if (!checkChildren(newModel)) {
            checkSelf(newModel)
            model = newModel
        }
    }

    private fun checkSelf(newModel: Model) {
        val oldModel = model
        watchers.forEach { element ->
            val getter = element.accessor
            val new = getter(newModel)
            if (oldModel == null || element.diff(getter(oldModel), new)) {
                element.callback(new)
            }
        }
    }

    private fun checkChildren(newModel: Model): Boolean {
        val recordedClass = childWatchers.keys.firstOrNull { it.isInstance(newModel) }
        if (recordedClass != null) {
            val targetWatcher = childWatchers[recordedClass]
            targetWatcher?.invoke(newModel)
            clearModels(targetWatcher)
            return true
        }

        clearModels(selectedChild = null)
        return false
    }

    private fun clearModels(selectedChild: ModelWatcher<Any>?) {
        if (selectedChild == null) {
            // Clear children only
            childWatchers.values.forEach {
                clear()
            }
        } else {
            // Clear children and self
            childWatchers.values.forEach {
                if (it !== selectedChild) {
                    clear()
                }
            }
            model = null
        }
        return
    }

    fun clear() {
        model = null
        childWatchers.values.forEach { it.clear() }
    }

    private class Watcher<Model, Field>(
        val accessor: (Model) -> Field,
        val callback: (Field) -> Unit,
        val diff: DiffStrategy<Field>
    )

    class Builder<Model : Any> @PublishedApi internal constructor() : WatchDsl<Model> {
        private val watchers = mutableListOf<Watcher<Model, Any?>>()
        @PublishedApi
        internal val childWatchers = hashMapOf<Class<Any>, ModelWatcher<Any>>()

        override fun <Field> watch(
            accessor: (Model) -> Field,
            diff: DiffStrategy<Field>,
            callback: (Field) -> Unit
        ) {
            watchers += Watcher(
                accessor,
                callback,
                diff
            ) as Watcher<Model, Any?>
        }

        @PublishedApi
        internal fun build(): ModelWatcher<Model> =
            ModelWatcher(watchers, childWatchers)
    }
}

internal interface BuilderBase<Model> {
    fun <Field> watch(
        accessor: (Model) -> Field,
        diff: DiffStrategy<Field> = byValue(),
        callback: (Field) -> Unit
    )
}

package com.badoo.mvicore

class ModelWatcher<Model : Any> private constructor(
    private val watchers: List<Watcher<Model, Any?>>,
    private val childWatchers: Map<Class<out Model>, ModelWatcher<out Model>>
) {
    private var model: Model? = null

    operator fun invoke(newModel: Model) {
        triggerChildren(newModel)

        triggerSelf(newModel)
        model = newModel
    }

    private fun triggerSelf(newModel: Model) {
        val oldModel = model
        watchers.forEach { element ->
            val getter = element.accessor
            val new = getter(newModel)
            if (oldModel == null || element.diff(getter(oldModel), new)) {
                element.callback(new)
            }
        }
    }

    private fun triggerChildren(newModel: Model) {
        val recordedClass = childWatchers.keys.firstOrNull { it.isInstance(newModel) }
        val targetWatcher = childWatchers[recordedClass] as? ModelWatcher<Model>
        targetWatcher?.invoke(newModel)
        clearNotMatchedChildren(selectedChild = targetWatcher)
    }

    private fun clearNotMatchedChildren(selectedChild: ModelWatcher<Model>?) {
        childWatchers.values.forEach {
            if (it !== selectedChild) {
                it.clear()
            }
        }
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

    @ModelWatcherDsl
    class Builder<Model : Any> @PublishedApi internal constructor() : BuilderBase<Model>, WatchDsl<Model> {
        private val watchers = mutableListOf<Watcher<Model, Any?>>()
        @PublishedApi
        internal val childWatchers = hashMapOf<Class<out Model>, ModelWatcher<out Model>>()

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

        inline fun <reified SubModel : Model> type(block: ModelWatcher.Builder<SubModel>.() -> Unit) {
            val childWatcher = modelWatcher(block)
            childWatchers[SubModel::class.java] = childWatcher
        }

        inline fun <reified SubModel : Model> objectType(noinline block: (SubModel) -> Unit) {
            type<SubModel> {
                watch({ it }, byRef(), block)
            }
        }

        @PublishedApi
        internal fun build(): ModelWatcher<Model> =
            ModelWatcher(watchers, childWatchers)
    }
}

@DslMarker
annotation class ModelWatcherDsl

internal interface BuilderBase<Model> {
    fun <Field> watch(
        accessor: (Model) -> Field,
        diff: DiffStrategy<Field> = byValue(),
        callback: (Field) -> Unit
    )
}

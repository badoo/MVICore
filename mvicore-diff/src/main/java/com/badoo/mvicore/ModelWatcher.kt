package com.badoo.mvicore

class ModelWatcher<Model> private constructor(
    private val watchers: List<Watcher<Model, Any?>>
) {
    private var model: Model? = null

    operator fun invoke(newModel: Model) {
        val oldModel = model
        watchers.forEach { element ->
            val getter = element.accessor
            val new = getter(newModel)
            if (oldModel == null || element.diff(getter(oldModel), new)) {
                element.callback(new)
            }
        }

        model = newModel
    }

    private class Watcher<Model, Field>(
        val accessor: (Model) -> Field,
        val callback: (Field) -> Unit,
        val diff: DiffStrategy<Field>
    )

    class Builder<Model> @PublishedApi internal constructor() {
        private val watchers = mutableListOf<Watcher<Model, Any?>>()

        fun <Field> watch(
            accessor: (Model) -> Field,
            diff: DiffStrategy<Field> = byValue(),
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
            ModelWatcher(watchers)

        /*
         * Syntactic sugar around watch (scoped inside the builder)
         */

        operator fun <Field> ((Model) -> Field).invoke(callback: (Field) -> Unit) {
            watch(this, callback = callback)
        }

        infix fun <Field> ((Model) -> Field).using(pair: Pair<DiffStrategy<Field>, (Field) -> Unit>) {
            watch(this, pair.first, pair.second)
        }

        operator fun <Field> (DiffStrategy<Field>).invoke(callback: (Field) -> Unit) =
            this to callback

        infix fun <Field1, Field2> ((Model) -> Field1).or(f: (Model) -> Field2): DiffStrategy<Model> =
            { old, new -> this(old) != this(new) || f(old) != f(new) }

        infix fun <Field1, Field2> ((Model) -> Field1).and(f: (Model) -> Field2): DiffStrategy<Model> =
            { old, new -> this(old) != this(new) && f(old) != f(new) }

        operator fun DiffStrategy<Model>.invoke(callback: (Model) -> Unit) {
            watch(
                accessor = { it },
                diff = this,
                callback = callback
            )
        }
    }
}

inline fun <Model> modelWatcher(init: ModelWatcher.Builder<Model>.() -> Unit): ModelWatcher<Model> =
    ModelWatcher.Builder<Model>()
        .apply(init)
        .build()

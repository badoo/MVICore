package com.badoo.mvicore

inline fun <Model : Any> modelWatcher(init: ModelWatcher.Builder<Model>.() -> Unit): ModelWatcher<Model> =
    ModelWatcher.Builder<Model>()
        .apply(init)
        .build()

internal interface WatchDsl<Model> : BuilderBase<Model> {
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

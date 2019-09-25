package com.badoo.mvicore

inline fun <reified SubModel : Any> ModelWatcher.Builder<*>.type(block: ModelWatcher.Builder<SubModel>.() -> Unit) {
    val childWatcher = modelWatcher(block)
    childWatchers[SubModel::class.java as Class<Any>] = childWatcher as ModelWatcher<Any>
}

inline fun <reified SubModel : Any> ModelWatcher.Builder<*>.objectType(noinline block: (SubModel) -> Unit) {
    type<SubModel> {
        watch({ it }, byRef(), block)
    }
}

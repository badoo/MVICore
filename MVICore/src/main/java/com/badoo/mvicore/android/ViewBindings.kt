package com.badoo.mvicore.android

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.lifecycle.Lifecycle

abstract class ViewBindings<T>(
    binderLifecycle: Lifecycle? = null
) {

    protected val binder = Binder(binderLifecycle)

    abstract fun setup(view: T)

    fun dispose() {
        binder.dispose()
    }
}

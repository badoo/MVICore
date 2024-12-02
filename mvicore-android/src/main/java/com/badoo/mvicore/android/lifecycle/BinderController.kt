package com.badoo.mvicore.android.lifecycle

import androidx.lifecycle.Lifecycle
import com.badoo.binder.Binder

class BinderController {

    private val binders = mutableListOf<Binder>()

    fun createDestroy(lifecycle: Lifecycle, f: Binder.() -> Unit) {
        Binder(CreateDestroyBinderLifecycle(lifecycle)).apply(f)
            .also { binders.add(it) }
    }

    fun startStop(lifecycle: Lifecycle, f: Binder.() -> Unit) {
        Binder(StartStopBinderLifecycle(lifecycle)).apply(f)
            .also { binders.add(it) }
    }

    fun resumePause(lifecycle: Lifecycle, f: Binder.() -> Unit) {
        Binder(ResumePauseBinderLifecycle(lifecycle)).apply(f)
            .also { binders.add(it) }
    }

    fun drain() {
        binders.forEach { it.drain() }
    }
}

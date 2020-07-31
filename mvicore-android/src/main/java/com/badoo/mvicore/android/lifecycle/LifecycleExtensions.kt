package com.badoo.mvicore.android.lifecycle

import android.arch.lifecycle.Lifecycle
import com.badoo.binder.Binder

fun Lifecycle.createDestroy(f: Binder.() -> Unit) {
    Binder(CreateDestroyBinderLifecycle(this)).apply(f)
}

fun Lifecycle.startStop(f: Binder.() -> Unit) {
    Binder(StartStopBinderLifecycle(this)).apply(f)
}

fun Lifecycle.resumePause(f: Binder.() -> Unit) {
    Binder(ResumePauseBinderLifecycle(this)).apply(f)
}

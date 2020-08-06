package com.badoo.mvicore.android

import android.arch.lifecycle.LifecycleOwner
import com.badoo.mvicore.binder.Binder

abstract class AndroidBindings<T : Any>(
    lifecycleOwner: LifecycleOwner
) {
    protected val binder = Binder(
        lifecycle = AndroidBinderLifecycle(
            androidLifecycle = lifecycleOwner.lifecycle
        )
    )

    abstract fun setup(view: T)
}

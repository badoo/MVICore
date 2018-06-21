package com.badoo.mvicore.android

import android.arch.lifecycle.LifecycleOwner
import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle
import android.arch.lifecycle.Lifecycle as AndroidLifecycle

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

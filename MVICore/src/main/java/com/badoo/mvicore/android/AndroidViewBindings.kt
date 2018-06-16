package com.badoo.mvicore.android

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle

abstract class AndroidViewBindings<T : LifecycleOwner>(
    lifecycleOwner: T
) : ViewBindings<T>(
    binderLifecycle = AndroidBinderLifecycle(androidLifecycle = lifecycleOwner.lifecycle)
)

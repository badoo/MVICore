package com.badoo.mvicore.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class TestLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this)

    var state: Lifecycle.State
        get() = registry.currentState
        set(value) {
            registry.currentState = value
        }

    override fun getLifecycle(): Lifecycle = registry
}

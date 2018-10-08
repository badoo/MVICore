package com.badoo.mvicore.android.lifecycle

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle


class CreateDestroyBinderLifecycle(
    androidLifecycle: AndroidLifecycle
): BaseAndroidBinderLifecycle(
    androidLifecycle,
    { sendEvent ->
        object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                sendEvent(BinderLifecycle.Event.BEGIN)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                sendEvent(BinderLifecycle.Event.END)
            }
        }
    }
)

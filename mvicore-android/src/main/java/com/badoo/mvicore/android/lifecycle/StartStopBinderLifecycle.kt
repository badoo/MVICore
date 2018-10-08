package com.badoo.mvicore.android.lifecycle

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle


class StartStopBinderLifecycle(
    androidLifecycle: AndroidLifecycle
): BaseAndroidBinderLifecycle(
    androidLifecycle,
    { sendEvent ->
        object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                sendEvent(BinderLifecycle.Event.BEGIN)
            }

            override fun onStop(owner: LifecycleOwner) {
                sendEvent(BinderLifecycle.Event.END)
            }
        }
    }
)

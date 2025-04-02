package com.badoo.mvicore.android.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.binder.lifecycle.Lifecycle as BinderLifecycle

class CreateDestroyBinderLifecycle(
    androidLifecycle: AndroidLifecycle
) : BaseAndroidBinderLifecycle(
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

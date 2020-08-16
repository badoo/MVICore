package com.badoo.mvicore.android.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle as AndroidLifecycle
import androidx.lifecycle.LifecycleOwner
import com.badoo.binder.lifecycle.Lifecycle as BinderLifecycle

class ResumePauseBinderLifecycle(
    androidLifecycle: AndroidLifecycle
): BaseAndroidBinderLifecycle(
    androidLifecycle,
    { sendEvent ->
        object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                sendEvent(BinderLifecycle.Event.BEGIN)
            }

            override fun onPause(owner: LifecycleOwner) {
                sendEvent(BinderLifecycle.Event.END)
            }
        }
    }
)

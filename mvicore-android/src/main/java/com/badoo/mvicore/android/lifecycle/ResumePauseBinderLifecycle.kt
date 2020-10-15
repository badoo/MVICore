package com.badoo.mvicore.android.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle as AndroidLifecycle
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

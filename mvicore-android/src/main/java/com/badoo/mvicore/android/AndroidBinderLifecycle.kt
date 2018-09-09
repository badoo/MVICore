package com.badoo.mvicore.android

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.ObservableSource
import io.reactivex.subjects.BehaviorSubject
import android.arch.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle


class AndroidBinderLifecycle(
    androidLifecycle: AndroidLifecycle,
    private val subject: BehaviorSubject<BinderLifecycle.Event> = BehaviorSubject.create()
) : BinderLifecycle,
    ObservableSource<BinderLifecycle.Event> by subject,
    LifecycleObserver {

    init {
        androidLifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                subject.onNext(BinderLifecycle.Event.BEGIN)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                subject.onNext(BinderLifecycle.Event.END)
            }
        })
    }
}

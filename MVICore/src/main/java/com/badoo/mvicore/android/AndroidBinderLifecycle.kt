package com.badoo.mvicore.android

import android.arch.lifecycle.Lifecycle as AndroidLifecycle
import android.arch.lifecycle.Lifecycle.Event.ON_CREATE
import android.arch.lifecycle.Lifecycle.Event.ON_DESTROY
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle
import io.reactivex.ObservableSource
import io.reactivex.subjects.PublishSubject


class AndroidBinderLifecycle(
    androidLifecycle: AndroidLifecycle,
    private val subject: PublishSubject<BinderLifecycle.Event> = PublishSubject.create()
) : BinderLifecycle,
    ObservableSource<BinderLifecycle.Event> by subject,
    LifecycleObserver {

    init {
        androidLifecycle.addObserver(this)
    }

    @OnLifecycleEvent(ON_CREATE)
    fun onCreated() = subject.onNext(BinderLifecycle.Event.BEGIN)

    @OnLifecycleEvent(ON_DESTROY)
    fun onDestroyed() = subject.onNext(BinderLifecycle.Event.END)
}

package com.badoo.mvicore2.lifecycle.internal

import android.arch.lifecycle.Lifecycle.Event.ON_CREATE
import android.arch.lifecycle.Lifecycle.Event.ON_DESTROY
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.badoo.mvicore2.lifecycle.Lifecycle
import com.badoo.mvicore2.lifecycle.Lifecycle.Event
import io.reactivex.ObservableSource
import io.reactivex.subjects.PublishSubject
import android.arch.lifecycle.Lifecycle as AndroidLifecycle

class Android(
        androidLifecycle: AndroidLifecycle,
        private val subject: PublishSubject<Event> = PublishSubject.create()
) : Lifecycle, ObservableSource<Event> by subject, LifecycleObserver {

    init {
        androidLifecycle.addObserver(this)
    }

    @OnLifecycleEvent(ON_CREATE)
    fun onCreated() = subject.onNext(Event.START)

    @OnLifecycleEvent(ON_DESTROY)
    fun onDestroyed() = subject.onNext(Event.STOP)
}

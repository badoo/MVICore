package com.badoo.mvicore.android.lifecycle

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleObserver
import io.reactivex.ObservableSource
import io.reactivex.subjects.BehaviorSubject
import android.arch.lifecycle.Lifecycle as AndroidLifecycle
import com.badoo.mvicore.binder.lifecycle.Lifecycle as BinderLifecycle

abstract class BaseAndroidBinderLifecycle(
    androidLifecycle: AndroidLifecycle,
    observerFactory: ((BinderLifecycle.Event) -> Unit) -> DefaultLifecycleObserver,
    private val subject: BehaviorSubject<BinderLifecycle.Event> = BehaviorSubject.create()
): BinderLifecycle,
    ObservableSource<BinderLifecycle.Event> by subject,
    LifecycleObserver {

    init {
        androidLifecycle.addObserver(observerFactory(subject::onNext))
    }
}

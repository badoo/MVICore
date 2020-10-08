package com.badoo.binder.lifecycle

import com.badoo.binder.lifecycle.Lifecycle.Event
import com.badoo.binder.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.binder.lifecycle.Lifecycle.Event.END
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ManualLifecycle(private val subject : BehaviorSubject<Event> = BehaviorSubject.create()) : Lifecycle by Lifecycle.wrap(subject) {

    fun begin() = subject.onNext(BEGIN)
    fun end() = subject.onNext(END)
}

package com.badoo.mvicore.binder.lifecycle

import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.binder.lifecycle.Lifecycle.Event.END
import io.reactivex.subjects.PublishSubject

class ManualLifecycle(private val subject : PublishSubject<Event> = PublishSubject.create()) : Lifecycle by Lifecycle.wrap(subject) {

    fun begin() = subject.onNext(BEGIN)
    fun end() = subject.onNext(END)
}

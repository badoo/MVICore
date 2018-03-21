package com.badoo.mvicore2.lifecycle

import com.badoo.mvicore2.lifecycle.Lifecycle.Event
import com.badoo.mvicore2.lifecycle.Lifecycle.Event.CREATE
import com.badoo.mvicore2.lifecycle.Lifecycle.Event.DESTROY
import io.reactivex.subjects.PublishSubject

class TestLifecycle(private val subject : PublishSubject<Event> = PublishSubject.create()) : Lifecycle by Lifecycle.wrap(subject){

    fun start() = subject.onNext(CREATE)
    fun stop() = subject.onNext(DESTROY)
}

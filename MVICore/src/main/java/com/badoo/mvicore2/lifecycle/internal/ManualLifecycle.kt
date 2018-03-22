package com.badoo.mvicore2.lifecycle.internal

import com.badoo.mvicore2.lifecycle.Lifecycle
import com.badoo.mvicore2.lifecycle.Lifecycle.Event
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class ManualLifecycle(private val subject: PublishSubject<Event> = PublishSubject.create()) : Lifecycle.Manual,
        Lifecycle by Lifecycle.custom(subject),
        Observer<Event> by subject

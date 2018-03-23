package com.badoo.mvicore2.lifecycle.internal

import com.badoo.mvicore2.lifecycle.Lifecycle
import com.badoo.mvicore2.utils.andThenEmmit
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.ObservableSource

class FromCompletableSource(source: CompletableSource) :
        Lifecycle,
        ObservableSource<Lifecycle.Event> by Completable.wrap(source).andThenEmmit(Lifecycle.Event.STOP)



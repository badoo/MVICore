package com.badoo.mvicore2.binder.internal

import com.badoo.mvicore2.binder.Binder
import com.badoo.mvicore2.lifecycle.Lifecycle
import com.badoo.mvicore2.lifecycle.Lifecycle.Event.START
import com.badoo.mvicore2.lifecycle.Lifecycle.Event.STOP
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

internal class ScopedBinding<T : Any>(private val scopeProvider: Lifecycle) : Binder.OneWayBinding<T, T> {

    override fun bind(source: ObservableSource<T>, observer: Consumer<T>) {
        Observable.wrap(scopeProvider)
                .takeUntil { it == STOP }
                .flatMap { if (it == START) source else Observable.empty() }
                .subscribe(observer)
    }

    override fun bind(pair: Pair<ObservableSource<T>, Consumer<T>>) = bind(pair.first, pair.second)
}

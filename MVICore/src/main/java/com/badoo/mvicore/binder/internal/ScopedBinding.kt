package com.badoo.mvicore.binder.internal

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.lifecycle.Lifecycle
import com.badoo.mvicore.lifecycle.Lifecycle.Event.DESTROY
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer

internal class ScopedBinding<T : Any>(private val scopeProvider: Lifecycle) : Binder.OneWayBinding<T, T> {

    override fun bind(source: ObservableSource<T>, observer: Observer<T>) =
            Observable.wrap(scopeProvider).takeUntil { it == DESTROY }.flatMap { source }.subscribe(observer)

    override fun bind(pair: Pair<ObservableSource<T>, Observer<T>>) = bind(pair.first, pair.second)

}

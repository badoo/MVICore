package com.badoo.mvicore.binder.internal

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.lifecycle.Lifecycle
import com.badoo.mvicore.lifecycle.Lifecycle.Event.DESTROY
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

internal class ScopedBinding<T : Any>(private val scopeProvider: Lifecycle) : Binder.OneWayBinding<T, T> {

    override fun bind(source: ObservableSource<T>, consumer: Consumer<T>) {
        Observable.wrap(scopeProvider).takeUntil { it == DESTROY }.flatMap { source }
            .subscribe(consumer)
    }

    override fun bind(pair: Pair<ObservableSource<T>, Consumer<T>>) = bind(pair.first, pair.second)
}

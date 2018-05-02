package com.badoo.mvicore.binder.internal

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.lifecycle.Lifecycle
import com.badoo.mvicore.lifecycle.Lifecycle.Event.CREATE
import com.badoo.mvicore.lifecycle.Lifecycle.Event.DESTROY
import io.reactivex.Observable.empty
import io.reactivex.Observable.wrap
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

internal class ScopedBinding<T : Any>(private val scopeProvider: Lifecycle) : Binder.OneWayBinding<T, T> {

    override fun bind(source: ObservableSource<T>, consumer: Consumer<T>) {
        wrap(scopeProvider).switchMap {
            when (it) {
                CREATE -> source
                DESTROY -> empty()
            }
        }.subscribe(consumer)
    }

    override fun bind(pair: Pair<ObservableSource<T>, Consumer<T>>) = bind(pair.first, pair.second)
}

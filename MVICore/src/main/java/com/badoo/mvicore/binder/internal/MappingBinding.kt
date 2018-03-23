package com.badoo.mvicore.binder.internal

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.Transformer
import com.badoo.mvicore.extension.mapNotNull
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer

internal class MappingBinding<Emitted : Any, Observed : Any>(
    private val wrapped: Binder.OneWayBinding<Observed, Observed>,
    private val transformer: Transformer<Emitted, Observed>
) : Binder.OneWayBinding<Emitted, Observed> {

    override fun bind(source: ObservableSource<Emitted>, observer: Observer<Observed>) =
            Observable.wrap(source)
                    .mapNotNull(transformer)
                    .let { wrapped.bind(it to observer) }

    override fun bind(pair: Pair<ObservableSource<Emitted>, Observer<Observed>>) = bind(pair.first, pair.second)

}

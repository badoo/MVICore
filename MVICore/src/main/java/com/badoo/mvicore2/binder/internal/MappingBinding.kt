package com.badoo.mvicore2.binder.internal

import com.badoo.mvicore2.binder.Binder
import com.badoo.mvicore2.binder.Transformer
import com.badoo.mvicore2.utils.mapNotNull
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

internal class MappingBinding<Emitted : Any, Observed : Any>(
        private val wrapped: Binder.OneWayBinding<Observed, Observed>,
        private val transformer: Transformer<Emitted, Observed>
) : Binder.OneWayBinding<Emitted, Observed> {

    override fun bind(source: ObservableSource<Emitted>, observer: Consumer<Observed>) =
            Observable.wrap(source)
                    .mapNotNull(transformer)
                    .let { wrapped.bind(it to observer) }

    override fun bind(pair: Pair<ObservableSource<Emitted>, Consumer<Observed>>) = bind(pair.first, pair.second)
}

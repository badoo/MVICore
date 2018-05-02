package com.badoo.mvicore2.binder.internal

import com.badoo.mvicore2.binder.Processor
import com.badoo.mvicore2.binder.Transformer
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

internal class MappingProcessor<In, Out>(
        private val transformer : Transformer<In, Out>,
        private val relay : PublishRelay<In> = PublishRelay.create()
) : Processor<In, Out>,
        Consumer<In> by relay,
        ObservableSource<Out> by relay.map(transformer)
{
}
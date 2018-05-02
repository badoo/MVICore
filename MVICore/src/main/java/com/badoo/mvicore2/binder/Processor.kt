package com.badoo.mvicore2.binder

import com.badoo.mvicore2.binder.internal.MappingProcessor
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Processor<In, Out> : ObservableSource<Out>, Consumer<In> {

    companion object {
        fun <In, Out> mapping(transformer: Transformer<In, Out>): Processor<In, Out> =
                MappingProcessor(transformer)
    }
}

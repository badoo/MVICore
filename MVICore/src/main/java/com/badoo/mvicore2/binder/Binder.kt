package com.badoo.mvicore2.binder

import com.badoo.mvicore2.binder.internal.ScopedBinder
import com.badoo.mvicore2.lifecycle.Lifecycle
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Binder {

    fun <Emitted : Any, Observed : Any> oneWay(a2b: Transformer<Emitted, Observed>): OneWayBinding<Emitted, Observed>

    fun <LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any> twoWay(
            a2b: Transformer<LeftOut, RightIn>,
            b2a: Transformer<RightOut, LeftIn>
    ): TwoWayBinding<LeftIn, LeftOut, RightIn, RightOut>

    interface OneWayBinding<A : Any, B : Any> {

        fun bind(source: ObservableSource<A>, observer: Consumer<B>)
        fun bind(pair: Pair<ObservableSource<A>, Consumer<B>>)
    }

    interface TwoWayBinding<LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any> {
        fun bind(left: Processor<LeftIn, LeftOut>, right: Processor<RightIn, RightOut>)
        fun bind(pair: Pair<Processor<LeftIn, LeftOut>, Processor<RightIn, RightOut>>)
    }

    companion object {

        fun from(lifecycle: Lifecycle): Binder = ScopedBinder(lifecycle)
    }
}
typealias Transformer<From, To> = (From) -> To?

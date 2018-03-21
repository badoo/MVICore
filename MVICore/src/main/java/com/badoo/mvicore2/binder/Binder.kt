package com.badoo.mvicore2.binder

import com.badoo.mvicore2.binder.internal.ScopedBinder
import com.badoo.mvicore2.lifecycle.Lifecycle
import io.reactivex.ObservableSource
import io.reactivex.Observer

interface Binder {

    fun <Emitted : Any, Observed : Any> oneWay(a2b: Transformer<Emitted, Observed>): OneWayBinding<Emitted, Observed>

    fun <LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any> twoWay(
            a2b: Transformer<LeftOut, RightIn>,
            b2a: Transformer<RightOut, LeftIn>
    ): TwoWayBinding<LeftIn, LeftOut, RightIn, RightOut>

    interface OneWayBinding<A : Any, B : Any> {

        fun bind(source: ObservableSource<A>, observer: Observer<B>)
        fun bind(pair: Pair<ObservableSource<A>, Observer<B>>)
    }

    interface TwoWayBinding<LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any> {
        fun bind(left: Bindable<LeftIn, LeftOut>, right: Bindable<RightIn, RightOut>)
        fun bind(pair: Pair<Bindable<LeftIn, LeftOut>, Bindable<RightIn, RightOut>>)
    }

    companion object {

        fun from(lifecycle: Lifecycle): Binder = ScopedBinder(lifecycle)
    }
}
typealias Transformer<From, To> = (From) -> To?

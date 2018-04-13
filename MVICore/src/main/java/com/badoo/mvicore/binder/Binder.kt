package com.badoo.mvicore.binder

import com.badoo.mvicore.binder.internal.ScopedBinder
import com.badoo.mvicore.lifecycle.Lifecycle
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Binder {

    fun <Out : Any, In : Any> bind(connection: Connection<Out, In>)

    fun <Emitted : Any, Observed : Any> oneWay(a2b: Transformer<Emitted, Observed>): OneWayBinding<Emitted, Observed>

    fun <LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any> twoWay(
        a2b: Transformer<LeftOut, RightIn>,
        b2a: Transformer<RightOut, LeftIn>
    ): TwoWayBinding<LeftIn, LeftOut, RightIn, RightOut>

    interface OneWayBinding<A : Any, B : Any> {
        fun bind(source: ObservableSource<A>, consumer: Consumer<B>): Unit
        fun bind(pair: Pair<ObservableSource<A>, Consumer<B>>): Unit
    }

    interface TwoWayBinding<LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any> {
        fun bind(left: Bindable<LeftIn, LeftOut>, right: Bindable<RightIn, RightOut>): Unit
        fun bind(pair: Pair<Bindable<LeftIn, LeftOut>, Bindable<RightIn, RightOut>>): Unit
    }

    companion object {
        fun from(lifecycle: Lifecycle): Binder = ScopedBinder(lifecycle)
    }
}

typealias Endpoints<Out, In> = Pair<ObservableSource<Out>, Consumer<In>>
typealias Transformer<From, To> = (From) -> To?
data class Connection<From, To>(
    val from: ObservableSource<From>,
    val to: Consumer<To>,
    val transformer: (From) -> To?
)

infix fun <Out, In> Endpoints<Out, In>.using(transformer: Transformer<Out, In>) =
    Connection(
        from = this.first,
        to = this.second,
        transformer = transformer
    )

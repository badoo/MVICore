package com.badoo.mvicore.binder.internal

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.lifecycle.Lifecycle
import com.badoo.mvicore.binder.Transformer

internal class ScopedBinder(private val provider: Lifecycle) : Binder {

    override fun <Out : Any, In : Any> bind(connection: Connection<Out, In>) =
        oneWay(connection.transformer).bind(connection.from, connection.to)

    override fun <A : Any, B : Any> oneWay(a2b: Transformer<A, B>): Binder.OneWayBinding<A, B> = MappingBinding(ScopedBinding(provider), a2b)

    override fun <InA : Any, OutA : Any, InB : Any, OutB : Any> twoWay(
        a2b: Transformer<OutA, InB>,
        b2a: Transformer<OutB, InA>
    ): Binder.TwoWayBinding<InA, OutA, InB, OutB> = DefaultTwoWayBinding(oneWay(a2b), oneWay(b2a))

}

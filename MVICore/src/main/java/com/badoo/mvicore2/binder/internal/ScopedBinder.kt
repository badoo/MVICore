package com.badoo.mvicore2.binder.internal

import com.badoo.mvicore2.binder.Binder
import com.badoo.mvicore2.lifecycle.Lifecycle
import com.badoo.mvicore2.binder.Transformer

internal class ScopedBinder(private val provider: Lifecycle) : Binder {
    override fun <A : Any, B : Any> oneWay(a2b: Transformer<A, B>): Binder.OneWayBinding<A, B> = MappingBinding(ScopedBinding(provider), a2b)

    override fun <InA : Any, OutA : Any, InB : Any, OutB : Any> twoWay(
            a2b: Transformer<OutA, InB>,
            b2a: Transformer<OutB, InA>
    ): Binder.TwoWayBinding<InA, OutA, InB, OutB> = DefaultTwoWayBinding(oneWay(a2b), oneWay(b2a))

}

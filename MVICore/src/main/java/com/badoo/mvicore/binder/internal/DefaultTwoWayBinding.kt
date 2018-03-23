package com.badoo.mvicore.binder.internal

import com.badoo.mvicore.binder.Bindable
import com.badoo.mvicore.binder.Binder

internal class DefaultTwoWayBinding<LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any>(
    private val bindingA2B: Binder.OneWayBinding<LeftOut, RightIn>,
    private val bindingB2A: Binder.OneWayBinding<RightOut, LeftIn>
) : Binder.TwoWayBinding<LeftIn, LeftOut, RightIn, RightOut> {

    override fun bind(pair: Pair<Bindable<LeftIn, LeftOut>, Bindable<RightIn, RightOut>>) {
        bind(pair.first, pair.second)
    }

    override fun bind(left: Bindable<LeftIn, LeftOut>, right: Bindable<RightIn, RightOut>) {
        bindingA2B.bind(left to right)
        bindingB2A.bind(right to left)
    }
}

package com.badoo.mvicore2.binder.internal

import com.badoo.mvicore2.binder.Processor
import com.badoo.mvicore2.binder.Binder

internal class DefaultTwoWayBinding<LeftIn : Any, LeftOut : Any, RightIn : Any, RightOut : Any>(
        private val bindingA2B: Binder.OneWayBinding<LeftOut, RightIn>,
        private val bindingB2A: Binder.OneWayBinding<RightOut, LeftIn>
) : Binder.TwoWayBinding<LeftIn, LeftOut, RightIn, RightOut> {

    override fun bind(pair: Pair<Processor<LeftIn, LeftOut>, Processor<RightIn, RightOut>>) {
        bind(pair.first, pair.second)
    }

    override fun bind(left: Processor<LeftIn, LeftOut>, right: Processor<RightIn, RightOut>) {
        bindingA2B.bind(left to right)
        bindingB2A.bind(right to left)
    }
}

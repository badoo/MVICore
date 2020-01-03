package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Source

interface Connector<Out, In> {
    operator fun invoke(source: Source<out Out>): Source<In>
}

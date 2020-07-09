package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Source

interface Connector<in Out, out In> {
    operator fun invoke(source: Source<Out>): Source<In>
}

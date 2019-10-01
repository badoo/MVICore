package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Source

interface Connector<Out, In>: (Source<out Out>) -> Source<In>

package com.badoo.mvicore.binder.lifecycle.internal

import com.badoo.mvicore.binder.lifecycle.Lifecycle
import io.reactivex.ObservableSource

internal class FromObservableSource(
    source: ObservableSource<Lifecycle.Event>
) : Lifecycle, ObservableSource<Lifecycle.Event> by source

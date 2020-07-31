package com.badoo.binder.lifecycle.internal

import com.badoo.binder.lifecycle.Lifecycle
import io.reactivex.ObservableSource

internal class FromObservableSource(
    source: ObservableSource<Lifecycle.Event>
) : Lifecycle, ObservableSource<Lifecycle.Event> by source

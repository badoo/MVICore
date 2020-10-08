package com.badoo.binder.lifecycle.internal

import com.badoo.binder.lifecycle.Lifecycle
import io.reactivex.rxjava3.core.ObservableSource

internal class FromObservableSource(
    source: ObservableSource<Lifecycle.Event>
) : Lifecycle, ObservableSource<Lifecycle.Event> by source

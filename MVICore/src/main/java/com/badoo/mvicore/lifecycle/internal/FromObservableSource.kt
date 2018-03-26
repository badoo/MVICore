package com.badoo.mvicore.lifecycle.internal

import com.badoo.mvicore.lifecycle.Lifecycle
import io.reactivex.ObservableSource

internal class FromObservableSource(source: ObservableSource<Lifecycle.Event>) : Lifecycle, ObservableSource<Lifecycle.Event> by source

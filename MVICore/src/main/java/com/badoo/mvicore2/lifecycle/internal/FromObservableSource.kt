package com.badoo.mvicore2.lifecycle.internal

import com.badoo.mvicore2.lifecycle.Lifecycle
import io.reactivex.ObservableSource

internal class FromObservableSource(source: ObservableSource<Lifecycle.Event>) : Lifecycle, ObservableSource<Lifecycle.Event> by source

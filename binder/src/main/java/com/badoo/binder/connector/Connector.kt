package com.badoo.binder.connector

import io.reactivex.rxjava3.core.ObservableSource

interface Connector<Out, In>: (ObservableSource<out Out>) -> ObservableSource<In>

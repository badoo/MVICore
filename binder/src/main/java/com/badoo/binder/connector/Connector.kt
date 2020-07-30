package com.badoo.binder.connector

import io.reactivex.ObservableSource

interface Connector<Out, In>: (ObservableSource<out Out>) -> ObservableSource<In>

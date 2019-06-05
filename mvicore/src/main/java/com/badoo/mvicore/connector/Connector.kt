package com.badoo.mvicore.connector

import io.reactivex.ObservableSource

interface Connector<Out, In>: (ObservableSource<Out>) -> ObservableSource<In>

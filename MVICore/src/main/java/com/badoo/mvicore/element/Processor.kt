package com.badoo.mvicore.element

import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Processor<In, Out> : Consumer<In>, ObservableSource<Out> {

}


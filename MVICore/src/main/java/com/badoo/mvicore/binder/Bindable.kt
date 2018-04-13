package com.badoo.mvicore.binder

import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Bindable<In, Out> : ObservableSource<Out>, Consumer<In>
package com.badoo.mvicore.element

import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.functions.Consumer

interface Store<Wish : Any, State : Any> : Consumer<Wish>, ObservableSource<State> {

    val state: State
}

package com.badoo.mvicore.element

import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Store<Wish : Any, State : Any> : Consumer<Wish>, ObservableSource<State> {

    val state: State
}

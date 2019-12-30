package com.badoo.mvicore.rx.feature

import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

interface Feature<Wish : Any, State : Any, News : Any> : Consumer<Wish>, ObservableSource<State> {
    val news: ObservableSource<News>
}


package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Store
import io.reactivex.ObservableSource
import io.reactivex.disposables.Disposable

interface Feature<Wish : Any, State : Any, News : Any> : Store<Wish, State>, Disposable {

    val news: ObservableSource<News>
}

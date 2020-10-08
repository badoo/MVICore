package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Store
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.disposables.Disposable

interface Feature<Wish : Any, State : Any, News : Any> : Store<Wish, State>, Disposable {

    val news: ObservableSource<News>
}

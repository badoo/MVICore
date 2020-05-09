package com.badoo.mvicore.rx.feature

import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.feature.BaseFeature
import com.badoo.mvicore.rx.element.Actor
import com.badoo.mvicore.rx.element.Bootstrapper
import com.badoo.mvicore.rx.element.NewsPublisher
import com.badoo.mvicore.rx.element.PostProcessor
import com.badoo.mvicore.rx.element.Reducer
import com.badoo.mvicore.rx.toCommonObserver
import com.badoo.mvicore.rx.toObservable
import com.badoo.mvicore.rx.toSource
import io.reactivex.ObservableSource
import io.reactivex.Observer

abstract class BaseFeature<Action : Any, Wish : Any, Effect : Any, State : Any, News : Any> (
    initialState: State,
    private val wishToAction: (Wish) -> Action,
    private val actor: Actor<State, Action, Effect>,
    private val reducer: Reducer<State, Effect>,
    private val bootstrapper: Bootstrapper<Action>? = null,
    private val newsPublisher: NewsPublisher<Action, Effect, State, News>? = null,
    private val postProcessor: PostProcessor<Action, Effect, State>? = null
): Feature<Wish, State, News> {
    private val delegate = object : BaseFeature<Action, Wish, Effect, State, News>(
        initialState = initialState,
        wishToAction = wishToAction,
        actor = ActorAdapter(actor),
        reducer = reducer,
        bootstrapper = bootstrapper?.let { BootstrapperAdapter(it) },
        newsPublisher = newsPublisher,
        postProcessor = postProcessor
    ) {  }

    override fun accept(wish: Wish) {
        delegate.accept(wish)
    }

    override val news: ObservableSource<News>
        get() = delegate.news.toObservable()

    override fun subscribe(observer: Observer<in State>) {
        delegate.connect(observer.toCommonObserver())
    }

    private class ActorAdapter<State, Action, Effect>(
        private val delegate: Actor<State, Action, Effect>
    ) : com.badoo.mvicore.common.element.Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Source<out Effect> =
            delegate.invoke(state, action).toSource()
    }

    private class BootstrapperAdapter<Action>(
        private val delegate: Bootstrapper<Action>
    ): com.badoo.mvicore.common.element.Bootstrapper<Action> {
        override fun invoke(): Source<Action> =
            delegate.invoke().toSource()
    }

    override fun dispose() {
        delegate.cancel()
    }

    override fun isDisposed(): Boolean =
        delegate.isCancelled
}

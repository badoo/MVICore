package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.CompositeCancellable
import com.badoo.mvicore.common.Observer
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.element.Actor
import com.badoo.mvicore.common.element.Bootstrapper
import com.badoo.mvicore.common.element.NewsPublisher
import com.badoo.mvicore.common.element.PostProcessor
import com.badoo.mvicore.common.element.Reducer
import com.badoo.mvicore.common.source

abstract class BaseFeature<in Action : Any, in Wish : Any, in Effect : Any, out State : Any, out News : Any> (
    initialState: State,
    private val wishToAction: (Wish) -> Action,
    private val actor: Actor<State, Action, Effect>,
    private val reducer: Reducer<State, Effect>,
    private val bootstrapper: Bootstrapper<Action>? = null,
    private val newsPublisher: NewsPublisher<Action, Effect, State, News>? = null,
    private val postProcessor: PostProcessor<Action, Effect, State>? = null
): Feature<Wish, State, News> {
    private val actionSource = source<Action>()
    private val stateSource = source(initialState)
    private val newsSource = source<News>()
    private val cancellables = CompositeCancellable()

    init {
        cancellables += actionSource.connect { action ->
            val oldState = state
            cancellables += actor.invoke(oldState, action)
                .connect { effect ->
                    val newState = reducer(state, effect)
                    stateSource.accept(newState)
                    newsPublisher?.invoke(oldState, action, effect, newState)?.let {
                        newsSource.accept(it)
                    }
                    postProcessor?.invoke(oldState, action, effect, newState)?.let(actionSource::accept)
                }
        }
        cancellables += bootstrapper?.invoke()?.connect(actionSource)
    }

    override fun accept(wish: Wish) {
        val action = wishToAction(wish)
        actionSource.accept(action)
    }

    override fun connect(observer: Observer<State>) = stateSource.connect(observer)

    override val isCancelled: Boolean
        get() = cancellables.isCancelled

    override fun cancel() {
        cancellables.cancel()
    }

    val state: State
        get() = stateSource.value!!

    override val news: Source<News>
        get() = newsSource
}

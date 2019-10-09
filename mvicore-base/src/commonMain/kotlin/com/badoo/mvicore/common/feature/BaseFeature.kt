package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.element.Actor
import com.badoo.mvicore.common.element.Bootstrapper
import com.badoo.mvicore.common.element.Feature
import com.badoo.mvicore.common.element.NewsPublisher
import com.badoo.mvicore.common.element.PostProcessor
import com.badoo.mvicore.common.element.Reducer
import com.badoo.mvicore.common.source

open class BaseFeature<Action : Any, Wish : Any, Effect : Any, State : Any, News : Any> (
    initialState: State,
    private val bootstrapper: Bootstrapper<Action>? = null,
    private val wishToAction: (Wish) -> Action,
    private val actor: Actor<State, Action, Effect>,
    private val reducer: Reducer<State, Effect>,
    private val newsPublisher: NewsPublisher<Action, Effect, State, News>? = null,
    private val postProcessor: PostProcessor<Action, Effect, State>? = null
): Feature<Wish, State, News> {
    private val actionSource = source<Action>()
    internal val effectSource = source<Effect>()
    private val stateSource = source(initialState)
    private val newsSource = source<News>()

    init {
        bootstrapper?.invoke()?.connect(actionSource)
        actionSource.connect { action ->
            val oldState = state
            actor.invoke(oldState, action)
                .connect { effect ->
                    val newState = reducer(this.state, effect)
                    stateSource(newState)
                    newsPublisher?.invoke(oldState, action, effect, newState)?.let {
                        newsSource(it)
                    }
                    postProcessor?.invoke(oldState, action, effect, newState)?.let(actionSource::invoke)
                }
        }
    }

    override fun invoke(wish: Wish) {
        val action = wishToAction(wish)
        actionSource.invoke(action)
    }

    override fun connect(sink: Sink<State>) = stateSource.connect(sink)

    override fun cancel() {
        // TODO
    }

    val state: State
        get() = stateSource.value!!

    override val news: Source<News>
        get() = newsSource
}

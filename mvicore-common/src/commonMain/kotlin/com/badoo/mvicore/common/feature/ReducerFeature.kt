package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.element.Actor
import com.badoo.mvicore.common.element.Bootstrapper
import com.badoo.mvicore.common.element.NewsPublisher
import com.badoo.mvicore.common.element.Reducer
import com.badoo.mvicore.common.sources.ValueSource

open class ReducerFeature<in Wish : Any, out State : Any, out News : Any>(
    initialState: State,
    bootstrapper: Bootstrapper<Wish>? = null,
    reducer: Reducer<State, Wish>,
    newsPublisher: SimpleNewsPublisher<Wish, State, News>? = null
) : ActorReducerFeature<Wish, Wish, State, News>(
    initialState = initialState,
    actor = PassthroughActor(),
    bootstrapper = bootstrapper,
    reducer = reducer,
    newsPublisher = newsPublisher?.let { NoEffectNewsPublisher(it) }
) {
    private class PassthroughActor<in State : Any, Wish : Any> : Actor<State, Wish, Wish> {
        override fun invoke(state: State, wish: Wish): Source<Wish> =
            ValueSource(wish)
    }

    private class NoEffectNewsPublisher<in Wish : Any, in State : Any, out News: Any>(
        private val simpleNewsPublisher: SimpleNewsPublisher<Wish, State, News>
    ) : NewsPublisher<Wish, Wish, State, News> {
        override fun invoke(old: State, action: Wish, effect: Wish, new: State): News? =
            simpleNewsPublisher(old, action, new)
    }
}

typealias SimpleNewsPublisher<Wish, State, News> = (old: State, wish: Wish, state: State) -> News?

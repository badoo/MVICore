package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.element.Actor
import com.badoo.mvicore.common.element.Bootstrapper
import com.badoo.mvicore.common.element.NewsPublisher
import com.badoo.mvicore.common.element.Reducer

open class ActorReducerFeature<in Wish : Any, in Effect : Any, out State : Any, out News : Any>(
    initialState: State,
    bootstrapper: Bootstrapper<Wish>? = null,
    actor: Actor<State, Wish, Effect>,
    reducer: Reducer<State, Effect>,
    newsPublisher: NewsPublisher<Wish, Effect, State, News>? = null
) : BaseFeature<Wish, Wish, Effect, State, News>(
    initialState = initialState,
    bootstrapper = bootstrapper,
    wishToAction = { it },
    actor = actor,
    reducer = reducer,
    newsPublisher = newsPublisher
)

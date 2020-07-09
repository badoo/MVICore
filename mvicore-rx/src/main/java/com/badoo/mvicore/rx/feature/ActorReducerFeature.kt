package com.badoo.mvicore.rx.feature

import com.badoo.mvicore.rx.element.Actor
import com.badoo.mvicore.rx.element.Bootstrapper
import com.badoo.mvicore.rx.element.NewsPublisher
import com.badoo.mvicore.rx.element.Reducer

open class ActorReducerFeature<Wish : Any, Effect : Any, State : Any, News : Any>(
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

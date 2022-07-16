package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer

open class ActorReducerFeature<Wish : Any, in Effect : Any, State : Any, News : Any>(
    initialState: State,
    bootstrapper: Bootstrapper<Wish>? = null,
    actor: Actor<State, Wish, Effect>,
    reducer: Reducer<State, Effect>,
    newsPublisher: NewsPublisher<Wish, Effect, State, News>? = null,
    featureScheduler: FeatureScheduler? = null
) : BaseFeature<Wish, Wish, Effect, State, News>(
    initialState = initialState,
    bootstrapper = bootstrapper,
    wishToAction = { wish -> wish },
    actor = actor,
    reducer = reducer,
    newsPublisher = newsPublisher,
    featureScheduler = featureScheduler
)

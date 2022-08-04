package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer

/**
 * An implementation of a single threaded feature.
 *
 * Please be aware of the following threading behaviours based on whether a 'featureScheduler' is provided.
 *
 * No 'featureScheduler' provided:
 * The feature must execute on the thread that created the class. If the bootstrapper/actor observables
 * change to a different thread it is your responsibility to switch back to the feature's original
 * thread via observeOn, otherwise an exception will be thrown.
 *
 * 'featureScheduler' provided (this must be single threaded):
 * The feature does not have to execute on the thread that created the class. It automatically
 * switches to the feature scheduler thread when necessary.
 */
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

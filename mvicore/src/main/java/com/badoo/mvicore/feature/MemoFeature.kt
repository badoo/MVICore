package com.badoo.mvicore.feature

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
open class MemoFeature<State : Any>(
    initialState: State,
    featureScheduler: FeatureScheduler? = null
) : Feature<State, State, Nothing> by ReducerFeature<State, State, Nothing>(
    initialState = initialState,
    reducer = { _, effect -> effect },
    featureScheduler = featureScheduler
)

package com.badoo.mvicore.feature

open class MemoFeature<State : Any>(
    initialState: State,
    featureScheduler: BaseFeature.FeatureScheduler? = null
) : Feature<State, State, Nothing> by ReducerFeature<State, State, Nothing>(
    initialState = initialState,
    reducer = { _, effect -> effect },
    featureScheduler = featureScheduler
)

package com.badoo.mvicore.featurewithaction.implementation

import com.badoo.mvicore.feature.DefaultFeature

abstract class ActorReducerFeature<Wish : Any, Effect : Any, State : Any>(
    initialState: State,
    actor: Actor<State, Wish, Effect>,
    reducer: Reducer<State, Effect>
) : DefaultFeature<Wish, Wish, Effect, State>(
    initialState = initialState,
    wishToAction = { wish -> wish },
    actor = actor,
    reducer = reducer
)

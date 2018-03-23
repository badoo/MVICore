package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer

object DefaultFeatureFactory : FeatureFactory {

    override fun <Wish : Any, State : Any, Effect : Any> create(
        initialState: State,
        actor: Actor<Wish, State, Effect>,
        reducer: Reducer<State, Effect>
    ) =
        DefaultFeature(
            initialState = initialState,
            actor = actor,
            reducer = reducer
        )
}

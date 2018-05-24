package com.badoo.mvicore.feature.example

import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ReducerFeature
import com.badoo.mvicore.feature.example.ReducerFeatureExample.Wish
import com.badoo.mvicore.feature.example.ReducerFeatureExample.State

class ReducerFeatureExample : ReducerFeature<Wish, State>(
    initialState = State(),
    reducer = ReducerImpl()
) {
    data class State(
        val i: Int = 0
    )

    sealed class Wish {
        object PublicWish1 : Wish()
        object PublicWish2 : Wish()
        object PublicWish3 : Wish()
    }

    sealed class Effect {
        object SomeEffect1 : Effect()
        object SomeEffect2 : Effect()
        object SomeEffect3 : Effect()
    }

    class ReducerImpl : Reducer<State, Wish> {
        override fun invoke(state: State, wish: Wish): State {
            TODO("not implemented")
        }
    }
}

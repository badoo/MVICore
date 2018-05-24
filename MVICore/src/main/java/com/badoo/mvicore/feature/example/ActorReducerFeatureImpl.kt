package com.badoo.mvicore.feature.example

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.badoo.mvicore.feature.example.ActorReducerFeatureImpl.Effect
import com.badoo.mvicore.feature.example.ActorReducerFeatureImpl.State
import com.badoo.mvicore.feature.example.ActorReducerFeatureImpl.Wish
import io.reactivex.Observable

class ActorReducerFeatureImpl : ActorReducerFeature<Wish, Effect, State>(
    initialState = State(),
    actor = ActorImpl(),
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

    class ActorImpl : Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            Wish.PublicWish1 -> TODO()
            Wish.PublicWish2 -> TODO()
            Wish.PublicWish3 -> TODO()
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            TODO("not implemented")
        }
    }
}

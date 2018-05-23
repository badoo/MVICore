package com.badoo.mvicore.featurewithaction.implementation

import com.badoo.mvicore.feature.DefaultFeature
import com.badoo.mvicore.featurewithaction.implementation.SimpleFeature.Effect
import com.badoo.mvicore.featurewithaction.implementation.SimpleFeature.State
import com.badoo.mvicore.featurewithaction.implementation.SimpleFeature.Wish
import io.reactivex.Observable

class SimpleFeature : ActorReducerFeature<Wish, Effect, State>(
    initialState = State(),
    actor = Actor(),
    reducer = Reducer()
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

    class Actor : DefaultFeature.Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            Wish.PublicWish1 -> TODO()
            Wish.PublicWish2 -> TODO()
            Wish.PublicWish3 -> TODO()
        }
    }

    class Reducer : DefaultFeature.Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            TODO("not implemented")
        }
    }
}

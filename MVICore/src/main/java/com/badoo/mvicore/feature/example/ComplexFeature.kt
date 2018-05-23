package com.badoo.mvicore.featurewithaction.implementation

import com.badoo.mvicore.feature.DefaultFeature
import com.badoo.mvicore.featurewithaction.implementation.ComplexFeature.Action
import com.badoo.mvicore.featurewithaction.implementation.ComplexFeature.Effect
import com.badoo.mvicore.featurewithaction.implementation.ComplexFeature.State
import com.badoo.mvicore.featurewithaction.implementation.ComplexFeature.Wish
import io.reactivex.Observable

class ComplexFeature : DefaultFeature<Wish, Action, Effect, State>(
    initialState = State(),
    wishToAction = { wish -> Action.Execute(wish) },
    actor = Actor(),
    reducer = Reducer(),
    postProcessor = PostProcessorImpl()
) {
    data class State(
        val i: Int = 0
    )

    sealed class Wish {
        object PublicWish1 : Wish()
        object PublicWish2 : Wish()
        object PublicWish3 : Wish()
    }

    sealed class Action {
        data class Execute(val wish: Wish) : Action()
        object InvalidateCache : Action()
    }

    sealed class Effect {
        object SomeEffect1 : Effect()
        object SomeEffect2 : Effect()
        object SomeEffect3 : Effect()
    }

    class Actor : DefaultFeature.Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> = when (action) {
            is Action.Execute -> when (action.wish) {
                Wish.PublicWish1 -> TODO()
                Wish.PublicWish2 -> TODO()
                Wish.PublicWish3 -> TODO()
            }
            Action.InvalidateCache -> TODO()
        }
    }

    class Reducer : DefaultFeature.Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            TODO("not implemented")
        }
    }

    class PostProcessorImpl : PostProcessor<Action, Effect, State> {

        // do anything based on action (contains wish), effect, state
        override fun invoke(action: Action, effect: Effect, state: State): Action? {
            if (state.i == 101) {
                return Action.InvalidateCache
            }

            return null
        }
    }
}

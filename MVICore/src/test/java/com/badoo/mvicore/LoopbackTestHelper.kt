package com.badoo.mvicore

import com.badoo.mvicore.LoopbackTestHelper.Effect.Effect1
import com.badoo.mvicore.LoopbackTestHelper.Effect.Effect2
import com.badoo.mvicore.LoopbackTestHelper.Effect.Effect3
import com.badoo.mvicore.LoopbackTestHelper.Wish.Wish1
import com.badoo.mvicore.LoopbackTestHelper.Wish.Wish2
import com.badoo.mvicore.LoopbackTestHelper.Wish.Wish3
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import io.reactivex.Observable
import io.reactivex.Observable.just

class LoopbackTestHelper {
    companion object {
        val initialState = State("Initial state")
        val state1 = State("State 1")
        val state2 = State("State 2")
        val state3 = State("State 3")
    }

    data class State(
            val id: String
    )

    sealed class Wish {
        object Wish1 : Wish()
        object Wish2 : Wish()
        object Wish3 : Wish()
    }

    sealed class Effect {
        object Effect1 : Effect(), News
        object Effect2 : Effect()
        object Effect3 : Effect()
    }

    class LoopbackTestActor(
        private val invokeCallback: (wish: Wish, state: State) -> Unit
    )
        : Actor<Wish, State, Effect> {

        override fun invoke(wish: Wish, state: State): Observable<Effect> {
            invokeCallback.invoke(wish, state)

            return when (wish) {
                Wish1 -> just(Effect1)
                Wish2 -> just(Effect2)
                Wish3 -> just(Effect3)
            }
        }
    }

    class LoopbackTestReducer : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State =
            when (effect) {
                Effect1 -> state1
                Effect2 -> state2
                Effect3 -> state3
            }
    }
}

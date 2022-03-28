package com.bumble.binder.orderfailure.prebindevent

import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ReducerFeature
import com.bumble.binder.orderfailure.prebindevent.Feature2.State
import com.bumble.binder.orderfailure.prebindevent.Feature2.Wish

class Feature2 : ReducerFeature<Wish, State, Nothing>(
    initialState = State(),
    reducer = ReducerImpl()
) {

    data class State(val actionEnabled: Boolean = true)

    sealed class Wish {
        object Wish1 : Wish()
    }

    class ReducerImpl : Reducer<State, Wish> {

        override fun invoke(state: State, effect: Wish): State {
            return when (effect) {
                Wish.Wish1 -> state.copy(actionEnabled = false)
            }
        }
    }
}

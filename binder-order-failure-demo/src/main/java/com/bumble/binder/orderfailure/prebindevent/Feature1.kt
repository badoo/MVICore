package com.bumble.binder.orderfailure.prebindevent

import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ReducerFeature
import com.bumble.binder.orderfailure.prebindevent.Feature1.State
import com.bumble.binder.orderfailure.prebindevent.Feature1.Wish
import io.reactivex.Observable

class Feature1 : ReducerFeature<Wish, State, Nothing>(
    initialState = State(),
    bootstrapper = BootstrapperImpl(),
    reducer = ReducerImpl()
) {

    data class State(val text: String? = "")

    sealed class Wish {
        object Wish1 : Wish()
        object Wish2 : Wish()
    }

    class BootstrapperImpl : Bootstrapper<Wish> {

        override fun invoke(): Observable<out Wish> {
            return Observable
                .just(Wish.Wish1)
        }
    }

    class ReducerImpl : Reducer<State, Wish> {

        override fun invoke(state: State, effect: Wish): State =
            when (effect) {
                Wish.Wish1 -> state.copy(text = "Wish1")
                Wish.Wish2 -> state.copy(text = null)
            }
    }
}
package com.badoo.feature1

import com.badoo.feature1.Feature1.State
import com.badoo.feature1.Feature1.Wish
import com.badoo.feature1.Feature1.Wish.IncreaseCounter
import com.badoo.feature1.Feature1.Wish.SetActiveButton
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ReducerFeature

class Feature1 : ReducerFeature<Wish, State, Nothing>(
    initialState = State(),
    reducer = ReducerImpl()
) {
    data class State(
        val counter: Int = 0,
        val activeButtonIdx: Int? = null
    )

    sealed class Wish {
        object IncreaseCounter : Wish()
        data class SetActiveButton(val idx: Int) : Wish()
    }

    class ReducerImpl : Reducer<State, Wish> {
        override fun invoke(state: State, wish: Wish): State = when (wish) {
            IncreaseCounter -> state.copy(
                counter = state.counter + 1
            )
            is SetActiveButton -> state.copy(
                activeButtonIdx = if (wish.idx != state.activeButtonIdx) wish.idx else null
            )
        }
    }
}

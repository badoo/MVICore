package com.bumble.order.component

import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ReducerFeature

class Feature : ReducerFeature<Feature.Wish, Feature.State, Feature.News>(
    initialState = State(),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisher()
) {

    data class State(val score: Int = 0)

    sealed class Wish {
        object Point : Wish()
    }

    sealed class News {
        object MaxScoreReached : News()
    }

    class ReducerImpl : Reducer<State, Wish> {
        override fun invoke(state: State, effect: Wish): State {
            return when (effect) {
                Wish.Point -> state.copy(score = state.score.inc())
            }
        }
    }

    class NewsPublisher : SimpleNewsPublisher<Wish, State, News>() {
        override fun invoke(wish: Wish, state: State): News? {
            return if (state.score == SCORE_LIMIT) {
                News.MaxScoreReached
            } else {
                null
            }
        }
    }

    private companion object {
        const val SCORE_LIMIT = 5
    }
}
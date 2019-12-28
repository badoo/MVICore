package com.badoo.mvicore.common

import com.badoo.mvicore.common.element.Bootstrapper
import com.badoo.mvicore.common.element.Reducer
import com.badoo.mvicore.common.feature.ReducerFeature
import com.badoo.mvicore.common.sources.ValueSource
import kotlin.test.Test

class ReducerFeatureTest {
    @Test
    fun reducer_feature_emits_initial_state_on_connect() {
        val feature = TestReducerFeature()
        val sink = TestSink<String>()

        feature.connect(sink)

        sink.assertValues("")
    }

    @Test
    fun reducer_feature_emits_new_state_on_new_wish() {
        val feature = TestReducerFeature()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(0)
        sink.assertValues("", "0")
    }

    @Test
    fun reducer_feature_emits_new_state_on_new_wish_2() {
        val feature = TestReducerFeature()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(0)
        feature.invoke(1)
        sink.assertValues("", "0", "01")
    }

    @Test
    fun reducer_feature_emits_new_state_on_bootstrapper_action() {
        val feature = TestReducerFeatureWBootstrapper()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(1)
        sink.assertValues("0", "01")
    }

    @Test
    fun reducer_feature_emits_news_on_wish() {
        val feature = TestReducerFeatureWNews()
        val sink = TestSink<Int>()

        feature.news.connect(sink)

        feature.invoke(1)
        feature.invoke(1)
        sink.assertValues(1, 2)
    }

    class TestReducerFeature(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        reducer = reducer { state, wish ->
            state + wish.toString()
        }
    )

    class TestReducerFeatureWBootstrapper(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        bootstrapper = object : Bootstrapper<Int> {
            override fun invoke(): Source<Int> =
                ValueSource(0)
        },
        reducer = reducer { state, wish ->
            state + wish.toString()
        }
    )

    class TestReducerFeatureWNews(initialState: String = ""): ReducerFeature<Int, String, Int>(
        initialState = initialState,
        reducer = reducer { state, wish ->
            state + wish.toString()
        },
        newsPublisher = { _: String, _: Int, state: String ->
            if (state.isNotEmpty()) {
                state.length
            } else {
                null
            }
        }
    )
}

fun <State, Effect> reducer(block: (state: State, effect: Effect) -> State) =
    object : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = block(state, effect)
    }

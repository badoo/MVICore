package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.TestSink
import com.badoo.mvicore.common.assertValues
import com.badoo.mvicore.common.bootstrapper
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.reducer
import com.badoo.mvicore.common.sources.ValueSource
import com.badoo.reaktive.utils.freeze
import kotlin.test.Test

class ReducerFeatureTest {
    @Test
    fun reducer_feature_emits_initial_state_on_connect() {
        val feature = TestReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        sink.assertValues("")
    }

    @Test
    fun reducer_feature_emits_new_state_on_new_wish() {
        val feature = TestReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(0)
        sink.assertValues("", "0")
    }

    @Test
    fun reducer_feature_emits_new_state_on_new_wish_2() {
        val feature = TestReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(0)
        feature.invoke(1)
        sink.assertValues("", "0", "01")
    }

    @Test
    fun reducer_feature_emits_new_state_on_bootstrapper_action() {
        val feature = TestReducerFeatureWBootstrapper().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        feature.invoke(1)
        sink.assertValues("0", "01")
    }

    @Test
    fun reducer_feature_emits_news_on_wish() {
        val feature = TestReducerFeatureWNews().freeze()
        val sink = TestSink<Int>()

        feature.news.connect(sink)

        feature.invoke(1)
        feature.invoke(1)
        sink.assertValues(1, 2)
    }

    @Test
    fun reducer_feature_stop_processing_events_after_cancel() {
        val feature = TestReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)
        feature.cancel()

        feature.invoke(1)
        sink.assertValues("")
    }

    class TestReducerFeature(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        reducer = reducer { state, wish ->
            state + wish.toString()
        }
    )

    class TestReducerFeatureWBootstrapper(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        bootstrapper = bootstrapper { ValueSource(0) },
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



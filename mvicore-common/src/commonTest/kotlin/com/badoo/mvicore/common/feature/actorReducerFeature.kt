package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.TestSink
import com.badoo.mvicore.common.actor
import com.badoo.mvicore.common.assertValues
import com.badoo.mvicore.common.bootstrapper
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.newsPublisher
import com.badoo.mvicore.common.reducer
import com.badoo.mvicore.common.sources.ValueSource
import com.badoo.reaktive.utils.freeze
import kotlin.test.Test

class ActorReducerFeatureTest {

    @Test
    fun feature_emits_initial_state_on_connect() {
        val feature = TestActorReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        sink.assertValues("")
    }

    @Test
    fun feature_emits_states_on_each_wish() {
        val feature = TestActorReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)
        feature.accept(0)

        sink.assertValues("", "0")
    }

    @Test
    fun feature_emits_states_on_each_actor_emission() {
        val feature = TestActorReducerFeature().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)
        feature.accept(1)

        sink.assertValues("", "1", "12")
    }

    @Test
    fun feature_updates_states_on_init_with_bootstrapper() {
        val feature = TestActorReducerFeatureWBootstrapper().freeze()
        val sink = TestSink<String>()

        feature.connect(sink)

        sink.assertValues("0")
    }

    @Test
    fun feature_emits_news_for_each_state_update() {
        val feature = TestActorReducerFeatureWNews().freeze()
        val stateSink = TestSink<String>()
        val newsSink = TestSink<Int>()

        feature.news.connect(newsSink)
        feature.connect(stateSink)
        feature.accept(0)
        feature.accept(1)

        stateSink.assertValues("", "0", "01", "012")
        newsSink.assertValues(0, 2)
    }

    @Test
    fun feature_stops_emitting_after_cancel() {
        val feature = TestActorReducerFeatureWNews().freeze()
        val stateSink = TestSink<String>()
        val newsSink = TestSink<Int>()

        feature.news.connect(newsSink)
        feature.connect(stateSink)

        feature.accept(0)
        feature.cancel()

        feature.accept(1)

        stateSink.assertValues("", "0")
        newsSink.assertValues(0)

    }
}

class TestActorReducerFeature(initialState: String = ""): ActorReducerFeature<Int, Int, String, Nothing>(
    initialState = initialState,
    actor = actor { _, wish ->
        if (wish % 2 == 0) ValueSource(wish) else ValueSource(wish, wish + 1)
    },
    reducer = reducer { state, effect ->
        state + effect.toString()
    }
)

class TestActorReducerFeatureWBootstrapper(initialState: String = ""): ActorReducerFeature<Int, Int, String, Nothing>(
    initialState = initialState,
    actor = actor { _, wish ->
        if (wish % 2 == 0) ValueSource(wish) else ValueSource(wish, wish + 1)
    },
    reducer = reducer { state, effect ->
        state + effect.toString()
    },
    bootstrapper = bootstrapper {
        ValueSource(0)
    }
)

class TestActorReducerFeatureWNews(initialState: String = ""): ActorReducerFeature<Int, Int, String, Int>(
    initialState = initialState,
    actor = actor { _, wish ->
        if (wish % 2 == 0) ValueSource(wish) else ValueSource(wish, wish + 1)
    },
    reducer = reducer { state, effect ->
        state + effect.toString()
    },
    newsPublisher = newsPublisher { _, _, effect, _ ->
        if (effect % 2 == 0) effect else null
    }
)

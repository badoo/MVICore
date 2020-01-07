package com.badoo.mvicore.common.feature

import com.badoo.mvicore.common.TestSink
import com.badoo.mvicore.common.actor
import com.badoo.mvicore.common.assertValues
import com.badoo.mvicore.common.bootstrapper
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.newsPublisher
import com.badoo.mvicore.common.reducer
import com.badoo.mvicore.common.sources.ValueSource
import kotlin.test.Test
import kotlin.test.assertFailsWith

class BaseFeatureTest {

    @Test
    fun feature_emits_initial_state_on_connect() {
        val feature = TestBaseFeature()
        val sink = TestSink<String>()

        feature.connect(sink)

        sink.assertValues("")
    }

    @Test
    fun feature_emits_states_on_each_wish() {
        val feature = TestBaseFeature()
        val sink = TestSink<String>()

        feature.connect(sink)
        feature.invoke("0")

        sink.assertValues("", "0")
    }

    @Test
    fun feature_emits_states_after_crash() {
        val feature = TestBaseFeatureCrashingActor()
        val sink = TestSink<String>()

        feature.connect(sink)

        assertFailsWith(IllegalArgumentException::class) {
            feature.invoke("1")
        }
        feature.invoke("0")

        sink.assertValues("", "0")
    }

    @Test
    fun feature_emits_states_on_each_actor_emission() {
        val feature = TestBaseFeature()
        val sink = TestSink<String>()

        feature.connect(sink)
        feature.invoke("1")

        sink.assertValues("", "1", "12")
    }

    @Test
    fun feature_updates_states_on_init_with_bootstrapper() {
        val feature = TestBaseFeatureWBootstrapper()
        val sink = TestSink<String>()

        feature.connect(sink)

        sink.assertValues("0")
    }

    @Test
    fun feature_emits_news_for_each_state_update() {
        val feature = TestBaseFeatureWNews()
        val stateSink = TestSink<String>()
        val newsSink = TestSink<Int>()

        feature.news.connect(newsSink)
        feature.connect(stateSink)
        feature.invoke("0")
        feature.invoke("1")

        stateSink.assertValues("", "0", "01", "012")
        newsSink.assertValues(0, 2)
    }

    @Test
    fun feature_stops_emitting_after_cancel() {
        val feature = TestBaseFeatureWNews()
        val stateSink = TestSink<String>()
        val newsSink = TestSink<Int>()

        feature.news.connect(newsSink)
        feature.connect(stateSink)

        feature.invoke("0")
        feature.cancel()

        feature.invoke("1")

        stateSink.assertValues("", "0")
        newsSink.assertValues(0)

    }
}

class TestBaseFeature(initialState: String = ""): BaseFeature<Int, String, Int, String, Nothing>(
    initialState = initialState,
    wishToAction = { it.toInt() },
    actor = actor { _, wish ->
        if (wish % 2 == 0) ValueSource(wish) else ValueSource(wish, wish + 1)
    },
    reducer = reducer { state, effect ->
        state + effect.toString()
    }
)

class TestBaseFeatureWBootstrapper(initialState: String = ""): BaseFeature<Int, String, Int, String, Nothing>(
    initialState = initialState,
    wishToAction = { it.toInt() },
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

class TestBaseFeatureWNews(initialState: String = ""): BaseFeature<Int, String, Int, String, Int>(
    initialState = initialState,
    wishToAction = { it.toInt() },
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

class TestBaseFeatureCrashingActor(initialState: String = ""): BaseFeature<Int, String, Int, String, Nothing>(
    initialState = initialState,
    wishToAction = { it.toInt() },
    actor = actor { _, wish ->
        if (wish % 2 == 0) ValueSource(wish) else throw IllegalArgumentException("Wish is odd")
    },
    reducer = reducer { state, effect ->
        state + effect.toString()
    }
)

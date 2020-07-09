package com.badoo.mvicore.rx.feature

import com.badoo.mvicore.rx.element.Bootstrapper
import com.badoo.mvicore.rx.element.Reducer
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.observers.TestObserver
import org.junit.Test

class ReducerFeatureTest {
    @Test
    fun reducer_feature_emits_initial_state_on_connect() {
        val feature = TestReducerFeature()
        val sink = TestObserver<String>()

        feature.subscribe(sink)

        sink.assertValues("")
    }

    @Test
    fun reducer_feature_emits_new_state_on_new_wish() {
        val feature = TestReducerFeature()
        val sink = TestObserver<String>()

        feature.subscribe(sink)

        feature.accept(0)
        sink.assertValues("", "0")
    }

    @Test
    fun reducer_feature_emits_new_state_on_new_wish_2() {
        val feature = TestReducerFeature()
        val sink = TestObserver<String>()

        feature.subscribe(sink)

        feature.accept(0)
        feature.accept(1)
        sink.assertValues("", "0", "01")
    }

    @Test
    fun reducer_feature_emits_new_state_on_bootstrapper_action() {
        val feature = TestReducerFeatureWBootstrapper()
        val sink = TestObserver<String>()

        feature.subscribe(sink)

        feature.accept(1)
        sink.assertValues("0", "01")
    }

    @Test
    fun reducer_feature_emits_news_on_wish() {
        val feature = TestReducerFeatureWNews()
        val sink = TestObserver<Int>()

        feature.news.subscribe(sink)

        feature.accept(1)
        feature.accept(1)
        sink.assertValues(1, 2)
    }

    @Test
    fun reducer_feature_stop_processing_events_after_cancel() {
        val feature = TestReducerFeature()
        val sink = TestObserver<String>()

        feature.subscribe(sink)
        feature.dispose()

        feature.accept(1)
        sink.assertValues("")
    }

    class TestReducerFeature(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        reducer = object : Reducer<String, Int> {
            override fun invoke(state: String, wish: Int): String =
                state + wish.toString()
        }
    )

    class TestReducerFeatureWBootstrapper(initialState: String = ""): ReducerFeature<Int, String, Nothing>(
        initialState = initialState,
        bootstrapper = object : Bootstrapper<Int> {
            override fun invoke(): ObservableSource<out Int> =
                Observable.just(0)
        },
        reducer = object : Reducer<String, Int> {
            override fun invoke(state: String, wish: Int): String =
                state + wish.toString()
        }
    )

    class TestReducerFeatureWNews(initialState: String = ""): ReducerFeature<Int, String, Int>(
        initialState = initialState,
        reducer = object : Reducer<String, Int> {
            override fun invoke(state: String, wish: Int): String =
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



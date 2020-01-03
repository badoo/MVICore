package com.badoo.mvicore

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.element.Actor
import com.badoo.mvicore.common.element.Reducer
import com.badoo.mvicore.common.feature.ActorReducerFeature
import com.badoo.mvicore.common.sources.ValueSource
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole


@State(Scope.Benchmark)
open class CommonFeatureBench {
    class TestActorReducerFeature(initialState: String = ""): ActorReducerFeature<Int, Int, String, Nothing>(
        initialState = initialState,
        actor = object : Actor<String, Int, Int> {
            override fun invoke(state: String, action: Int): Source<out Int> =
                ValueSource(action)
        },
        reducer = object : Reducer<String, Int> {
            override fun invoke(state: String, effect: Int): String =
                state + effect
        }
    )

    private lateinit var feature: TestActorReducerFeature
    private lateinit var disposable: Cancellable

    @Setup(Level.Iteration)
    open fun setup(blackhole: Blackhole) {
        feature = TestActorReducerFeature()
        disposable = feature.connect { blackhole.consume(it) }
    }

    @Benchmark
    fun feature() {
        feature.invoke(1)
    }

    @TearDown(Level.Iteration)
    open fun tearDown() {
        disposable.cancel()
    }
}

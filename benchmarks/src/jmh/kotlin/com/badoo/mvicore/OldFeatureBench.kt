package com.badoo.mvicore

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole


@State(Scope.Benchmark)
open class OldFeatureBench {

    class TestActorReducerFeature(initialState: String = ""): ActorReducerFeature<Int, Int, String, Nothing>(
        initialState = initialState,
        actor = object : Actor<String, Int, Int> {
            override fun invoke(state: String, action: Int): Observable<out Int> =
                Observable.just(action)
        },
        reducer = object : Reducer<String, Int> {
            override fun invoke(state: String, effect: Int): String =
                state + effect
        }
    )

    private lateinit var feature: TestActorReducerFeature
    private lateinit var disposable: Disposable

    @Setup(Level.Iteration)
    open fun setup(blackhole: Blackhole) {
        feature = TestActorReducerFeature()
        disposable = Observable.wrap(feature).subscribe { blackhole.consume(it) }
    }

    @Benchmark
    fun feature() {
        feature.accept(1)
    }

    @TearDown(Level.Iteration)
    open fun tearDown() {
        disposable.dispose()
    }
}

package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.TrampolineFeatureSchedulerTest.TestFeature.Effect
import com.badoo.mvicore.feature.TrampolineFeatureSchedulerTest.TestFeature.State
import com.badoo.mvicore.feature.TrampolineFeatureSchedulerTest.TestFeature.Wish
import com.badoo.mvicore.feature.FeatureSchedulers.TrampolineFeatureScheduler
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class TrampolineFeatureSchedulerTest {

    @Test
    fun `ensure feature is testable with trampoline scheduler`() {
        val computationScheduler = TestScheduler()
        val feature = TestFeature(
            featureScheduler = TrampolineFeatureScheduler,
            computationScheduler = computationScheduler
        )
        val states = Observable.wrap(feature).test()

        feature.accept(Wish.Trigger)
        computationScheduler.advanceTimeBy(1, TimeUnit.MINUTES)

        assertEquals(true, states.values().last().mutated)
    }

    class TestFeature(
        featureScheduler: FeatureScheduler,
        computationScheduler: Scheduler = Schedulers.computation()
    ) : ActorReducerFeature<Wish, Effect, State, Nothing>(
        initialState = State(),
        actor = ActorImpl(scheduler = computationScheduler),
        reducer = ReducerImpl,
        featureScheduler = featureScheduler
    ) {
        sealed class Wish {
            object Trigger : Wish()
        }

        sealed class Effect {
            object Mutate : Effect()
        }

        data class State(val mutated: Boolean = false)

        class ActorImpl(private val scheduler: Scheduler) : Actor<State, Wish, Effect> {
            override fun invoke(state: State, wish: Wish): Observable<out Effect> =
                when (wish) {
                    Wish.Trigger -> Observable.timer(1, TimeUnit.MINUTES, scheduler)
                        .map { Effect.Mutate }
                }
        }

        object ReducerImpl : Reducer<State, Effect> {
            override fun invoke(state: State, effect: Effect): State =
                when (effect) {
                    Effect.Mutate -> state.copy(mutated = true)
                }
        }
    }
}

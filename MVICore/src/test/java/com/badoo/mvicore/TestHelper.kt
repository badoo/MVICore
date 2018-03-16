package com.badoo.mvicore

import com.badoo.mvicore.TestHelper.TestEffect.ConditionalThingHappened
import com.badoo.mvicore.TestHelper.TestEffect.FinishedAsync
import com.badoo.mvicore.TestHelper.TestEffect.InstantEffect
import com.badoo.mvicore.TestHelper.TestEffect.LoopbackEffect1
import com.badoo.mvicore.TestHelper.TestEffect.LoopbackEffect2
import com.badoo.mvicore.TestHelper.TestEffect.LoopbackEffect3
import com.badoo.mvicore.TestHelper.TestEffect.LoopbackEffectInitial
import com.badoo.mvicore.TestHelper.TestEffect.MultipleEffect1
import com.badoo.mvicore.TestHelper.TestEffect.MultipleEffect2
import com.badoo.mvicore.TestHelper.TestEffect.MultipleEffect3
import com.badoo.mvicore.TestHelper.TestEffect.StartedAsync
import com.badoo.mvicore.TestHelper.TestWish.FulfillableAsync
import com.badoo.mvicore.TestHelper.TestWish.FulfillableInstantly1
import com.badoo.mvicore.TestHelper.TestWish.FulfillableInstantly2
import com.badoo.mvicore.TestHelper.TestWish.IncreasCounterBy
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWish1
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWish2
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWish3
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWishInitial
import com.badoo.mvicore.TestHelper.TestWish.MaybeFulfillable
import com.badoo.mvicore.TestHelper.TestWish.TranslatesTo3Effects
import com.badoo.mvicore.TestHelper.TestWish.Unfulfillable
import com.badoo.mvicore.core.Engine
import com.badoo.mvicore.core.Feature
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Actor.Companion.combineEffects
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observer
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit

class TestHelper {
    companion object {
        const val initialCounter = 100
        const val initialLoading = false
        const val instantFulfillAmount1 = 2
        const val instantFulfillAmount2 = 100
        const val delayedFulfillAmount = 5
        const val divisorForModuloInConditionalWish = 3
        const val conditionalMultiplier = 10
        const val mockServerDelayMs: Long = 10
        val loopBackInitialState = TestState("Loopback initial state")
        val loopBackState1 = TestState("Loopback state 1")
        val loopBackState2 = TestState("Loopback state 2")
        val loopBackState3 = TestState("Loopback state 3")
    }

    data class TestState(
            val id: String = "",
            val useless: Int = initialCounter,
            val counter: Int = initialCounter,
            val loading: Boolean = false
    )

    sealed class TestWish {
        object Unfulfillable : TestWish()
        object MaybeFulfillable : TestWish()
        object FulfillableInstantly1 : TestWish()
        object FulfillableInstantly2 : TestWish()
        object FulfillableAsync : TestWish()
        object TranslatesTo3Effects : TestWish()
        object LoopbackWishInitial : TestWish()
        object LoopbackWish1 : TestWish()
        object LoopbackWish2 : TestWish()
        object LoopbackWish3 : TestWish()
        data class IncreasCounterBy(val value: Int) : TestWish()
    }

    sealed class TestEffect {
        object StartedAsync : TestEffect()
        data class InstantEffect(val amount: Int) : TestEffect()
        data class FinishedAsync(val amount: Int) : TestEffect()
        data class ConditionalThingHappened(val multiplier: Int) : TestEffect(), News
        object MultipleEffect1 : TestEffect()
        object MultipleEffect2 : TestEffect()
        object MultipleEffect3 : TestEffect()
        object LoopbackEffectInitial : TestEffect()
        object LoopbackEffect1 : TestEffect(), News
        object LoopbackEffect2 : TestEffect()
        object LoopbackEffect3 : TestEffect()
    }

    class TestActor(
        private val invocationCallback: (wish: TestWish, state: TestState) -> Unit,
        private val mockServerUseCase: MockServerUseCase = MockServerUseCase()
    ) : Actor<TestWish, TestState, TestEffect> {

        override fun invoke(wish: TestWish, state: TestState): Observable<TestEffect> {
            invocationCallback.invoke(wish, state)
            return when (wish) {
                Unfulfillable -> noop()
                MaybeFulfillable -> conditional(state)
                FulfillableInstantly1 -> fulfill(amount = instantFulfillAmount1)
                FulfillableInstantly2 -> fulfill(amount = instantFulfillAmount2)
                FulfillableAsync -> asyncJob()
                TranslatesTo3Effects -> emit3effects()
                LoopbackWishInitial -> just(LoopbackEffectInitial)
                LoopbackWish1 -> just(LoopbackEffect1)
                LoopbackWish2 -> just(LoopbackEffect2)
                LoopbackWish3 -> just(LoopbackEffect3)
                is IncreasCounterBy -> just(InstantEffect(amount = wish.value))
            }
        }

        private fun noop(): Observable<TestEffect> =
                Observable.empty()

        private fun conditional(state: TestState): Observable<TestEffect> =
            // depends on current state
            if (state.counter % divisorForModuloInConditionalWish == 0) just(ConditionalThingHappened(multiplier = conditionalMultiplier))
            else noop()

        private fun fulfill(amount: Int): Observable<TestEffect> =
            just(InstantEffect(amount))

        private fun asyncJob(): Observable<TestEffect> =
            combineEffects(
                immediate = StartedAsync,
                additional = mockServerUseCase
                    .execute()
                    .map { FinishedAsync(it) as TestEffect }
            )

        private fun emit3effects(): Observable<TestEffect> =
            just(
                MultipleEffect1,
                MultipleEffect2,
                MultipleEffect3
            )
    }

    class TestReducer : Reducer<TestState, TestEffect> {
        override fun invoke(state: TestState, effect: TestEffect): TestState =
            when (effect) {
                is StartedAsync -> state.copy(loading = true)
                is InstantEffect -> state.copy(counter = state.counter + effect.amount)
                is FinishedAsync -> state.copy(counter = state.counter + effect.amount, loading = false)
                is ConditionalThingHappened -> state.copy(counter = state.counter * effect.multiplier)
                MultipleEffect1 -> state
                MultipleEffect2 -> state
                MultipleEffect3 -> state
                LoopbackEffectInitial -> loopBackInitialState
                LoopbackEffect1 -> loopBackState1
                LoopbackEffect2 -> loopBackState2
                LoopbackEffect3 -> loopBackState3
            }
    }

    class TestFeature(
        engine: Engine<TestState, TestWish, TestEffect>,
        newsSubject: Observer<News>,
        actorInvocationLog: Subject<Pair<TestWish, TestState>>
    ) : Feature<TestState, TestWish, TestEffect>(
        engine = engine,
        initialState = TestState(
            counter = TestHelper.initialCounter,
            loading = TestHelper.initialLoading
        ),
        actor = TestActor(
            invocationCallback = { wish, state ->
                actorInvocationLog.onNext(wish to state)
            }
        ),
        reducer = TestReducer(),
        newsObserver = newsSubject
    )

    sealed class TestUiEvent {
        object ImportantButtonClicked : TestUiEvent()
        data class SpinnerValueChanged(val value: Int) : TestUiEvent()
    }

    data class TestViewModel(
        val counter: Int
    )

    class MockServerUseCase {
        fun execute(): Observable<Int> = just(delayedFulfillAmount)
            .delay(mockServerDelayMs, TimeUnit.MILLISECONDS)
    }
}

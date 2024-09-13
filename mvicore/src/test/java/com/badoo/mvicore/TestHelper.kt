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
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.empty
import io.reactivex.rxjava3.core.Observable.just
import io.reactivex.rxjava3.core.Scheduler
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

    class TestEmptyBootstrapper : Bootstrapper<TestWish> {
        override fun invoke(): Observable<out TestWish> = empty()
    }

    sealed class TestWish {
        data object Unfulfillable : TestWish()
        data object MaybeFulfillable : TestWish()
        data object FulfillableInstantly1 : TestWish()
        data object FulfillableInstantly2 : TestWish()
        data class FulfillableAsync(val delayMs: Long) : TestWish()
        data object TranslatesTo3Effects : TestWish()
        data object LoopbackWishInitial : TestWish()
        data object LoopbackWish1 : TestWish()
        data object LoopbackWish2 : TestWish()
        data object LoopbackWish3 : TestWish()
        data class IncreasCounterBy(val value: Int) : TestWish()
    }

    sealed class TestEffect {
        data object StartedAsync : TestEffect()
        data class InstantEffect(val amount: Int) : TestEffect()
        data class FinishedAsync(val amount: Int) : TestEffect()
        data class ConditionalThingHappened(val multiplier: Int) : TestEffect()
        data object MultipleEffect1 : TestEffect()
        data object MultipleEffect2 : TestEffect()
        data object MultipleEffect3 : TestEffect()
        data object LoopbackEffectInitial : TestEffect()
        data object LoopbackEffect1 : TestEffect()
        data object LoopbackEffect2 : TestEffect()
        data object LoopbackEffect3 : TestEffect()
    }

    sealed class TestNews {
        data object ConditionalThingHappened : TestNews()
        data object Loopback : TestNews()
    }

    class TestActor(
        private val invocationCallback: (wish: TestWish, state: TestState) -> Unit,
        private val asyncWorkScheduler: Scheduler
    ) : Actor<TestState, TestWish, TestEffect> {

        override fun invoke(state: TestState, wish: TestWish): Observable<TestEffect> {
            invocationCallback.invoke(wish, state)
            return when (wish) {
                Unfulfillable -> noop()
                MaybeFulfillable -> conditional(state)
                FulfillableInstantly1 -> fulfill(amount = instantFulfillAmount1)
                FulfillableInstantly2 -> fulfill(amount = instantFulfillAmount2)
                is FulfillableAsync -> asyncJob(wish)
                TranslatesTo3Effects -> emit3effects()
                LoopbackWishInitial -> just(LoopbackEffectInitial)
                LoopbackWish1 -> just(LoopbackEffect1)
                LoopbackWish2 -> just(LoopbackEffect2)
                LoopbackWish3 -> just(LoopbackEffect3)
                is IncreasCounterBy -> just(InstantEffect(amount = wish.value))
            }
        }

        private fun noop(): Observable<TestEffect> =
            empty()

        private fun conditional(state: TestState): Observable<TestEffect> =
            // depends on current state
            if (state.counter % divisorForModuloInConditionalWish == 0)
                just(ConditionalThingHappened(multiplier = conditionalMultiplier))
            else
                noop()

        private fun fulfill(amount: Int): Observable<TestEffect> =
            just(InstantEffect(amount))

        private fun asyncJob(wish: FulfillableAsync): Observable<TestEffect> =
            just(delayedFulfillAmount)
                .delay(wish.delayMs, TimeUnit.MILLISECONDS, asyncWorkScheduler)
                .map<TestEffect> { FinishedAsync(it) }
                .startWith(just(StartedAsync))

        private fun emit3effects(): Observable<TestEffect> =
            just(
                MultipleEffect1,
                MultipleEffect2,
                MultipleEffect3
            )
    }

    class TestReducer(private val invocationCallback: () -> Unit = {}) : Reducer<TestState, TestEffect> {
        override fun invoke(state: TestState, effect: TestEffect): TestState {
            invocationCallback()
            return when (effect) {
                is StartedAsync -> state.copy(loading = true)
                is InstantEffect -> state.copy(counter = state.counter + effect.amount)
                is FinishedAsync -> state.copy(
                    counter = state.counter + effect.amount,
                    loading = false
                )
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
    }

    class TestNewsPublisher : NewsPublisher<TestWish, TestEffect, TestState, TestNews> {
        override fun invoke(wish: TestWish, effect: TestEffect, state: TestState): TestNews? =
            when (effect) {
                is ConditionalThingHappened -> TestNews.ConditionalThingHappened
                LoopbackEffect1 -> TestNews.Loopback
                else -> null
            }
    }
}

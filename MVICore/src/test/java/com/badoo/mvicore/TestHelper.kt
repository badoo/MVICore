package com.badoo.mvicore

import com.badoo.mvicore.TestHelper.Effect.ConditionalThingHappened
import com.badoo.mvicore.TestHelper.Effect.FinishedAsync
import com.badoo.mvicore.TestHelper.Effect.InstantEffect
import com.badoo.mvicore.TestHelper.Effect.StartedAsync
import com.badoo.mvicore.TestHelper.Wish.FulfillableAsync
import com.badoo.mvicore.TestHelper.Wish.FulfillableInstantly1
import com.badoo.mvicore.TestHelper.Wish.FulfillableInstantly2
import com.badoo.mvicore.TestHelper.Wish.MaybeFulfillable
import com.badoo.mvicore.TestHelper.Wish.TranslatesTo3Effects
import com.badoo.mvicore.TestHelper.Wish.Unfulfillable
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Actor.Companion.combineEffects
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import io.reactivex.Observable
import io.reactivex.Observable.just
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
        const val playbackInitialCounter = 1000
    }

    data class State(
            val id: String = "",
            val useless: Int = initialCounter,
            val counter: Int = initialCounter,
            val loading: Boolean = false
    )

    sealed class Wish {
        object Unfulfillable : Wish()
        object MaybeFulfillable : Wish()
        object FulfillableInstantly1 : Wish()
        object FulfillableInstantly2 : Wish()
        object FulfillableAsync : Wish()
        object TranslatesTo3Effects : Wish()
    }

    sealed class Effect {
        object StartedAsync : Effect()
        data class InstantEffect(val amount: Int) : Effect()
        data class FinishedAsync(val amount: Int) : Effect()
        data class ConditionalThingHappened(val multiplier: Int) : Effect(), News
        object MultipleEffect1 : Effect()
        object MultipleEffect2 : Effect()
        object MultipleEffect3 : Effect()
    }

    class TestActor(
        private val mockServerUseCase: MockServerUseCase = MockServerUseCase()
    ) : Actor<Wish, State, Effect> {

        override fun invoke(wish: Wish, state: State): Observable<Effect> =
            when (wish) {
                Unfulfillable -> noop()
                MaybeFulfillable -> conditional(state)
                FulfillableInstantly1 -> fulfill(amount = instantFulfillAmount1)
                FulfillableInstantly2 -> fulfill(amount = instantFulfillAmount2)
                FulfillableAsync -> asyncJob()
                TranslatesTo3Effects -> emit3effects()
            }

        private fun noop(): Observable<Effect> =
                Observable.empty()

        private fun conditional(state: State): Observable<Effect> =
            // depends on current state
            if (state.counter % divisorForModuloInConditionalWish == 0) just(ConditionalThingHappened(multiplier = conditionalMultiplier))
            else noop()

        private fun fulfill(amount: Int): Observable<Effect> =
            just(InstantEffect(amount))

        private fun asyncJob(): Observable<Effect> =
            combineEffects(
                immediate = StartedAsync,
                additional = mockServerUseCase
                    .execute()
                    .map { FinishedAsync(it) as Effect }
            )

        private fun emit3effects(): Observable<Effect> =
            just(
                Effect.MultipleEffect1,
                Effect.MultipleEffect2,
                Effect.MultipleEffect3
            )
    }

    class TestReducer : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State =
            when (effect) {
                is StartedAsync -> state.copy(loading = true)
                is InstantEffect -> state.copy(counter = state.counter + effect.amount)
                is FinishedAsync -> state.copy(counter = state.counter + effect.amount, loading = false)
                is ConditionalThingHappened -> state.copy(counter = state.counter * effect.multiplier)
                else -> state.copy(
                    // to make sure state will pass through .distinctUntilChanged
                    useless = state.useless + 1
                )
            }
    }

    class MockServerUseCase {
        fun execute(): Observable<Int> = just(delayedFulfillAmount)
            .delay(mockServerDelayMs, TimeUnit.MILLISECONDS)
    }
}

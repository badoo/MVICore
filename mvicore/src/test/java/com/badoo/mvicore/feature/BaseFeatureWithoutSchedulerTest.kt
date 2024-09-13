package com.badoo.mvicore.feature

import com.badoo.mvicore.TestHelper
import com.badoo.mvicore.TestHelper.Companion.conditionalMultiplier
import com.badoo.mvicore.TestHelper.Companion.initialCounter
import com.badoo.mvicore.TestHelper.Companion.initialLoading
import com.badoo.mvicore.TestHelper.Companion.instantFulfillAmount1
import com.badoo.mvicore.TestHelper.TestNews
import com.badoo.mvicore.TestHelper.TestState
import com.badoo.mvicore.TestHelper.TestWish
import com.badoo.mvicore.TestHelper.TestWish.FulfillableAsync
import com.badoo.mvicore.TestHelper.TestWish.FulfillableInstantly1
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWish1
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWish2
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWish3
import com.badoo.mvicore.TestHelper.TestWish.LoopbackWishInitial
import com.badoo.mvicore.TestHelper.TestWish.MaybeFulfillable
import com.badoo.mvicore.TestHelper.TestWish.TranslatesTo3Effects
import com.badoo.mvicore.TestHelper.TestWish.Unfulfillable
import com.badoo.mvicore.extension.SameThreadVerifier
import com.badoo.mvicore.onNextEvents
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BaseFeatureWithoutSchedulerTest {
    private lateinit var feature: Feature<TestWish, TestState, TestNews>
    private lateinit var states: TestObserver<TestState>
    private lateinit var newsSubject: PublishSubject<TestNews>
    private lateinit var actorInvocationLog: PublishSubject<Pair<TestWish, TestState>>
    private lateinit var actorInvocationLogTest: TestObserver<Pair<TestWish, TestState>>
    private lateinit var actorScheduler: TestScheduler

    @BeforeEach
    fun prepare() {
        SameThreadVerifier.isEnabled = false

        newsSubject = PublishSubject.create()
        actorInvocationLog = PublishSubject.create()
        actorInvocationLogTest = actorInvocationLog.test()
        actorScheduler = TestScheduler()

        feature = BaseFeature(
            initialState = TestState(),
            wishToAction = { wish -> wish },
            actor = TestHelper.TestActor(
                { wish, state -> actorInvocationLog.onNext(wish to state) },
                actorScheduler
            ),
            reducer = TestHelper.TestReducer(),
            newsPublisher = TestHelper.TestNewsPublisher(),
            featureScheduler = null
        )

        val subscription = PublishSubject.create<TestState>()
        states = subscription.test()
        feature.subscribe(subscription)
        feature.news.subscribe(newsSubject)
    }

    @AfterEach
    fun teardown() {
        // Reset back to the default to ensure we don't introduce flaky behaviours
        SameThreadVerifier.isEnabled = true
    }

    @Test
    fun `if there are no wishes, feature only emits initial state`() {
        assertEquals(1, states.onNextEvents().size)
    }

    @Test
    fun `emitted initial state is correct`() {
        val state: TestState = states.onNextEvents().first() as TestState
        assertEquals(initialCounter, state.counter)
        assertEquals(initialLoading, state.loading)
    }

    @Test
    fun `there should be no state emission besides the initial one for unfulfillable wishes`() {
        feature.accept(Unfulfillable)
        feature.accept(Unfulfillable)
        feature.accept(Unfulfillable)

        assertEquals(1, states.onNextEvents().size)
    }

    @Test
    fun `there should be the same amount of states as wishes that translate 1 - 1 to effects plus one for initial state`() {
        val wishes = listOf<TestWish>(
            // all of them are mapped to 1 effect each
            FulfillableInstantly1,
            FulfillableInstantly1,
            FulfillableInstantly1
        )

        wishes.forEach { feature.accept(it) }

        assertEquals(1 + wishes.size, states.onNextEvents().size)
    }

    @Test
    fun `there should be 3 times as many states as wishes that translate 1 - 3 to effects plus one for initial state`() {
        val wishes = listOf<TestWish>(
            TranslatesTo3Effects,
            TranslatesTo3Effects,
            TranslatesTo3Effects
        )

        wishes.forEach { feature.accept(it) }

        assertEquals(1 + wishes.size * 3, states.onNextEvents().size)
    }

    @Test
    fun `last state correctly reflects expected changes in simple case`() {
        val wishes = listOf<TestWish>(
            FulfillableInstantly1,
            FulfillableInstantly1,
            FulfillableInstantly1
        )

        wishes.forEach { feature.accept(it) }

        val state = states.values().last()
        assertEquals(initialCounter + wishes.size * instantFulfillAmount1, state.counter)
        assertEquals(false, state.loading)
    }

    @Test
    fun `intermediate state matches expectations in async case`() {
        val wishes = listOf(
            FulfillableAsync(0)
        )

        wishes.forEach { feature.accept(it) }

        val state = states.values().last()
        assertEquals(true, state.loading)
        assertEquals(initialCounter, state.counter)
    }

    @Test
    fun `final state matches expectations in async case`() {
        val mockServerDelayMs: Long = 10

        val wishes = listOf(
            FulfillableAsync(mockServerDelayMs)
        )

        wishes.forEach { feature.accept(it) }

        actorScheduler.advanceTimeBy(mockServerDelayMs, TimeUnit.MILLISECONDS)

        val state = states.values().last()
        assertEquals(false, state.loading)
        assertEquals(initialCounter + TestHelper.delayedFulfillAmount, state.counter)
    }

    @Test
    fun `the number of state emissions should reflect the number of effects plus one for initial state in complex case`() {
        val wishes = listOf(
            FulfillableInstantly1,  // maps to 1 effect
            FulfillableInstantly1,  // maps to 1 effect
            MaybeFulfillable,       // maps to 0 in this case
            Unfulfillable,          // maps to 0
            FulfillableInstantly1,  // maps to 1
            FulfillableInstantly1,  // maps to 1
            MaybeFulfillable,       // maps to 1 in this case
            TranslatesTo3Effects    // maps to 3
        )

        wishes.forEach { feature.accept(it) }

        assertEquals(8 + 1, states.onNextEvents().size)
    }

    @Test
    fun `last state correctly reflects expected changes in complex case`() {
        val wishes = listOf(
            FulfillableInstantly1,  // should increase +2 (total: 102)
            FulfillableInstantly1,  // should increase +2 (total: 104)
            MaybeFulfillable,       // should not do anything in this state, as total of 2 is not divisible by 3
            Unfulfillable,          // should not do anything
            FulfillableInstantly1,  // should increase +2 (total: 106)
            FulfillableInstantly1,  // should increase +2 (total: 108)
            MaybeFulfillable,       // as total of 108 is divisible by 3, it should multiply by *10 (total: 1080)
            TranslatesTo3Effects    // should not affect state
        )

        wishes.forEach { feature.accept(it) }

        val state = states.values().last()
        assertEquals(
            (initialCounter + 4 * instantFulfillAmount1) * conditionalMultiplier,
            state.counter
        )
        assertEquals(false, state.loading)
    }

    @Test
    fun `loopback from news to multiple wishes has access to correct latest state`() {
        newsSubject.subscribe {
            if (it === TestNews.Loopback) {
                feature.accept(LoopbackWish2)
                feature.accept(LoopbackWish3)
            }
        }

        feature.accept(LoopbackWishInitial)
        feature.accept(LoopbackWish1)
        assertEquals(4, actorInvocationLogTest.onNextEvents().size)
        assertEquals(
            LoopbackWish1 to TestHelper.loopBackInitialState,
            actorInvocationLogTest.onNextEvents()[1]
        )
        assertEquals(
            LoopbackWish2 to TestHelper.loopBackState1,
            actorInvocationLogTest.onNextEvents()[2]
        )
        assertEquals(
            LoopbackWish3 to TestHelper.loopBackState2,
            actorInvocationLogTest.onNextEvents()[3]
        )
    }
}

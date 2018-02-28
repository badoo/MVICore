package com.badoo.mvicore.core

import com.badoo.mvicore.LoopbackTestHelper
import com.badoo.mvicore.TestHelper
import com.badoo.mvicore.TestHelper.Companion.conditionalMultiplier
import com.badoo.mvicore.TestHelper.Companion.initialCounter
import com.badoo.mvicore.TestHelper.Companion.initialLoading
import com.badoo.mvicore.TestHelper.Companion.instantFulfillAmount1
import com.badoo.mvicore.TestHelper.Effect.ConditionalThingHappened
import com.badoo.mvicore.TestHelper.State
import com.badoo.mvicore.TestHelper.TestActor
import com.badoo.mvicore.TestHelper.TestReducer
import com.badoo.mvicore.TestHelper.Wish
import com.badoo.mvicore.TestHelper.Wish.FulfillableAsync
import com.badoo.mvicore.TestHelper.Wish.FulfillableInstantly1
import com.badoo.mvicore.TestHelper.Wish.MaybeFulfillable
import com.badoo.mvicore.TestHelper.Wish.TranslatesTo3Effects
import com.badoo.mvicore.TestHelper.Wish.Unfulfillable
import com.badoo.mvicore.element.News
import com.badoo.mvicore.onNextEvents
import com.badoo.mvicore.overrideAssertsForTesting
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class DefaultFeatureTest {

    private lateinit var feature: Feature<State, Wish>
    private lateinit var states: TestObserver<State>
    private lateinit var news: TestObserver<News>
    private var newsSubject = PublishSubject.create<News>()

    @Before
    fun prepare() {
        MockitoAnnotations.initMocks(this)
        overrideAssertsForTesting(false)

        feature = DefaultFeature(Configuration(
                    initialState = State(
                        counter = TestHelper.initialCounter,
                        loading = TestHelper.initialLoading
                    ),
                    actor = TestActor(),
                    reducer = TestReducer(),
                    newsPublisher = { newsSubject.onNext(it) }
                )
        )

        states = feature.states.test()
        news = newsSubject.test()
    }

    @Test
    fun `if there are no wishes, feature only emits initial state`() {
        assertEquals(1, states.onNextEvents().size)
    }

    @Test
    fun `emitted initial state is correct`() {
        val state: State = states.onNextEvents().first() as State
        assertEquals(initialCounter, state.counter)
        assertEquals(initialLoading, state.loading)
    }

    @Test
    fun `there should be no state emission besides the initial one for unfulfillable wishes`() {
        feature.onWish(Unfulfillable)
        feature.onWish(Unfulfillable)
        feature.onWish(Unfulfillable)

        assertEquals(1, states.onNextEvents().size)
    }

    @Test
    fun `there should be the same amount of states as wishes that translate 1 - 1 to effects plus one for initial state`() {
        val wishes = listOf<Wish>(
                // all of them are mapped to 1 effect each
            FulfillableInstantly1,
            FulfillableInstantly1,
            FulfillableInstantly1
        )

        wishes.forEach { feature.onWish(it) }

        assertEquals(1 + wishes.size, states.onNextEvents().size)
    }

    @Test
    fun `there should be 3 times as many states as wishes that translate 1 - 3 to effects plus one for initial state`() {
        val wishes = listOf<Wish>(
                TranslatesTo3Effects,
                TranslatesTo3Effects,
                TranslatesTo3Effects
        )

        wishes.forEach { feature.onWish(it) }

        assertEquals(1 + wishes.size * 3, states.onNextEvents().size)
    }

    @Test
    fun `last state correctly reflects expected changes in simple case`() {
        val wishes = listOf<Wish>(
            FulfillableInstantly1,
            FulfillableInstantly1,
            FulfillableInstantly1
        )

        wishes.forEach { feature.onWish(it) }

        val state = states.onNextEvents().last() as State
        assertEquals(initialCounter + wishes.size * instantFulfillAmount1, state.counter)
        assertEquals(false, state.loading)
    }

    @Test
    fun `intermediate state matches expectations in async case`() {
        val wishes = listOf(
            FulfillableAsync
        )

        wishes.forEach { feature.onWish(it) }

        val state = states.onNextEvents().last() as State
        assertEquals(true, state.loading)
        assertEquals(initialCounter, state.counter)
    }

    @Test
    fun `final state matches expectations in async case`() {
        val wishes = listOf(
                FulfillableAsync
        )

        wishes.forEach { feature.onWish(it) }

        Thread.sleep(TestHelper.mockServerDelayMs + 200)

        val state = states.onNextEvents().last() as State
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

        wishes.forEach { feature.onWish(it) }

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

        wishes.forEach { feature.onWish(it) }

        val state = states.onNextEvents().last() as State
        assertEquals((initialCounter + 4 * instantFulfillAmount1) * conditionalMultiplier, state.counter)
        assertEquals(false, state.loading)
    }

    @Test
    fun `the number and type of news emitted should match expectations`() {
        val wishes = listOf(
            FulfillableInstantly1,  // no news
            FulfillableInstantly1,  // no news
            MaybeFulfillable,       // should not do anything in this state, no news
            Unfulfillable,          // should not do anything
            FulfillableInstantly1,  // no news
            FulfillableInstantly1,  // no news
            MaybeFulfillable,       // as total of 108 is divisible by 3, it should emit news
            TranslatesTo3Effects    // should not affect state
        )

        wishes.forEach { feature.onWish(it) }

        assertEquals(1, news.onNextEvents().size)
        assertEquals(true, news.onNextEvents().last() is ConditionalThingHappened)
    }

    @Test
    fun `loopback from news to multiple wishes has access to correct latest state`() {
        val invokeLog = PublishSubject.create<Pair<LoopbackTestHelper.Wish, LoopbackTestHelper.State>>()
        val invokeLogTest = invokeLog.test()
        val invokeCallback: (wish: LoopbackTestHelper.Wish, state: LoopbackTestHelper.State) -> Unit = {
            wish, state -> invokeLog.onNext(wish to state)
        }

        val newsSubject = PublishSubject.create<News>()
        val feature = DefaultFeature(Configuration(
                initialState = LoopbackTestHelper.initialState,
                actor = LoopbackTestHelper.LoopbackTestActor(invokeCallback),
                reducer = LoopbackTestHelper.LoopbackTestReducer(),
                newsPublisher = { news -> newsSubject.onNext(news) }
        ))

        newsSubject.subscribe {
            if (it is LoopbackTestHelper.Effect.Effect1) {
                feature.onWish(LoopbackTestHelper.Wish.Wish2)
                feature.onWish(LoopbackTestHelper.Wish.Wish3)
            }
        }

        feature.onWish(LoopbackTestHelper.Wish.Wish1)
        assertEquals(3, invokeLogTest.onNextEvents().size)
        assertEquals(LoopbackTestHelper.Wish.Wish1 to LoopbackTestHelper.initialState, invokeLogTest.onNextEvents()[0])
        assertEquals(LoopbackTestHelper.Wish.Wish2 to LoopbackTestHelper.state1, invokeLogTest.onNextEvents()[1])
        assertEquals(LoopbackTestHelper.Wish.Wish3 to LoopbackTestHelper.state2, invokeLogTest.onNextEvents()[2])
    }
}

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
import com.badoo.mvicore.utils.RxErrorRule
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxErrorRule::class)
class BaseFeatureWithSchedulerTest {
    private lateinit var feature: Feature<TestWish, TestState, TestNews>
    private lateinit var states: TestObserver<TestState>
    private lateinit var newsSubject: PublishSubject<TestNews>
    private lateinit var actorInvocationLog: PublishSubject<Pair<TestWish, TestState>>
    private lateinit var actorInvocationLogTest: TestObserver<Pair<TestWish, TestState>>
    private lateinit var actorScheduler: Scheduler
    private val featureScheduler = TestThreadFeatureScheduler()

    @BeforeEach
    fun prepare() {
        SameThreadVerifier.isEnabled = true

        newsSubject = PublishSubject.create()
        actorInvocationLog = PublishSubject.create()
        actorInvocationLogTest = actorInvocationLog.test()
        actorScheduler = TestScheduler()
    }

    private fun initFeature() {
        feature = BaseFeature(
            initialState = TestState(),
            bootstrapper = TestHelper.TestEmptyBootstrapper(),
            wishToAction = { wish -> wish },
            actor = TestHelper.TestActor(
                { wish, state ->
                    if (!featureScheduler.isOnFeatureThread) {
                        fail<Unit>("Actor was not invoked on the feature thread")
                    }
                    actorInvocationLog.onNext(wish to state)
                },
                actorScheduler
            ),
            reducer = TestHelper.TestReducer(invocationCallback = {
                if (!featureScheduler.isOnFeatureThread) {
                    fail<Unit>("Reducer was not invoked on the feature thread")
                }
            }),
            newsPublisher = TestHelper.TestNewsPublisher(),
            featureScheduler = featureScheduler
        )

        val subscription = PublishSubject.create<TestState>()
        states = subscription.test()
        feature.subscribe(subscription)
        feature.news.subscribe(newsSubject)
    }

    private fun initAndObserveFeature(): TestObserver<TestState> {
        initFeature()
        return Observable.wrap(feature).test()
    }

    @Test
    fun `if there are no wishes, feature only emits initial state`() {
        initFeature()
        assertEquals(1, states.onNextEvents().size)
    }

    @Test
    fun `emitted initial state is correct`() {
        initFeature()
        val state: TestState = states.onNextEvents().first() as TestState
        assertEquals(initialCounter, state.counter)
        assertEquals(initialLoading, state.loading)
    }

    @Test
    fun `there should be no state emission besides the initial one for unfulfillable wishes`() {
        initFeature()
        feature.accept(Unfulfillable)
        feature.accept(Unfulfillable)
        feature.accept(Unfulfillable)

        actorInvocationLogTest.awaitAndAssertCount(3)

        assertEquals(1, states.onNextEvents().size)
    }

    @Test
    fun `there should be the same amount of states as wishes that translate 1 - 1 to effects plus one for initial state`() {
        val testObserver = initAndObserveFeature()
        val wishes = listOf<TestWish>(
            // all of them are mapped to 1 effect each
            FulfillableInstantly1,
            FulfillableInstantly1,
            FulfillableInstantly1
        )

        wishes.forEach { feature.accept(it) }

        testObserver.awaitAndAssertCount(1 + wishes.size)
    }

    @Test
    fun `there should be 3 times as many states as wishes that translate 1 - 3 to effects plus one for initial state`() {
        val testObserver = initAndObserveFeature()
        val wishes = listOf<TestWish>(
            TranslatesTo3Effects,
            TranslatesTo3Effects,
            TranslatesTo3Effects
        )

        wishes.forEach { feature.accept(it) }

        testObserver.awaitAndAssertCount(1 + wishes.size * 3)
    }

    @Test
    fun `last state correctly reflects expected changes in simple case`() {
        val testObserver = initAndObserveFeature()
        val wishes = listOf<TestWish>(
            FulfillableInstantly1,
            FulfillableInstantly1,
            FulfillableInstantly1
        )

        wishes.forEach { feature.accept(it) }

        testObserver.awaitAndAssertCount(1 + wishes.size)
        val state = states.values().last()
        assertEquals(initialCounter + wishes.size * instantFulfillAmount1, state.counter)
        assertEquals(false, state.loading)
    }

    @Test
    fun `intermediate state matches expectations in async case`() {
        val testObserver = initAndObserveFeature()
        val wishes = listOf(
            FulfillableAsync(0)
        )

        wishes.forEach { feature.accept(it) }

        testObserver.awaitAndAssertCount(1 + wishes.size)
        val state = states.values().last()
        assertEquals(true, state.loading)
        assertEquals(initialCounter, state.counter)
    }

    @Test
    fun `final state matches expectations in async case`() {
        val testScheduler = TestScheduler()
        actorScheduler = testScheduler
        val testObserver = initAndObserveFeature()
        val mockServerDelayMs: Long = 10

        val wishes = listOf(
            FulfillableAsync(mockServerDelayMs)
        )

        wishes.forEach { feature.accept(it) }

        // Must wait until the loading state has started, otherwise the timer is advanced too soon.
        testObserver.awaitAndAssertCount(1 + wishes.size)
        testScheduler.advanceTimeBy(mockServerDelayMs, TimeUnit.MILLISECONDS)

        testObserver.awaitAndAssertCount(2 + wishes.size)
        val state = states.values().last()
        assertEquals(false, state.loading)
        assertEquals(initialCounter + TestHelper.delayedFulfillAmount, state.counter)
    }

    @Test
    fun `the number of state emissions should reflect the number of effects plus one for initial state in complex case`() {
        val testObserver = initAndObserveFeature()
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

        testObserver.awaitAndAssertCount(8 + 1)
    }

    @Test
    fun `last state correctly reflects expected changes in complex case`() {
        val testObserver = initAndObserveFeature()
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

        testObserver.awaitAndAssertCount(8 + 1)
        val state = states.values().last()
        assertEquals(
            (initialCounter + 4 * instantFulfillAmount1) * conditionalMultiplier,
            state.counter
        )
        assertEquals(false, state.loading)
    }

    @Test
    fun `loopback from news to multiple wishes has access to correct latest state`() {
        initAndObserveFeature()
        newsSubject.subscribe {
            if (it === TestNews.Loopback) {
                feature.accept(LoopbackWish2)
                feature.accept(LoopbackWish3)
            }
        }

        feature.accept(LoopbackWishInitial)
        feature.accept(LoopbackWish1)

        actorInvocationLogTest.awaitAndAssertCount(4)
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

    @Test
    fun `if feature created on different thread, feature scheduler accessed once for bootstrapping`() {
        initFeature()

        assertEquals(1, featureScheduler.schedulerInvocationCount)
    }

    @Test
    fun `if feature created on same thread, feature scheduler still accessed for bootstrapping`() {
        val latch = CountDownLatch(1)
        featureScheduler.testScheduler.scheduleDirect {
            initFeature()
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertEquals(1, featureScheduler.schedulerInvocationCount)
    }

    @Test
    fun `feature scheduler should be accessed 7 times when 3 async wishes invoked`() {
        actorScheduler = Schedulers.computation()

        val testObserver = initAndObserveFeature()

        feature.accept(FulfillableAsync(0))
        feature.accept(FulfillableAsync(0))
        feature.accept(FulfillableAsync(0))

        testObserver.awaitAndAssertCount(7)

        // Bootstrapper (1) is called on test thread and must be moved to feature thread
        // Each wish (3) is called on test thread and must be moved to feature thread
        // Each effect (3) is async and must be moved to feature thread
        assertEquals(7, featureScheduler.schedulerInvocationCount)
    }

    private fun <T> TestObserver<T>.awaitAndAssertCount(count: Int) {
        awaitCount(count)
        assertValueCount(count)
    }

    private class TestThreadFeatureScheduler : FeatureScheduler {
        val schedulerInvocationCount: Int
            get() = countingScheduler.interactionCount

        private val delegate by lazy {
            FeatureSchedulers.createFeatureScheduler("AsyncTestScheduler")
        }

        val testScheduler: Scheduler
            get() = delegate.scheduler

        private val countingScheduler: CountingScheduler by lazy {
            CountingScheduler(delegate = testScheduler)
        }

        override val scheduler: Scheduler
            get() = countingScheduler

        override val isOnFeatureThread: Boolean
            get() = delegate.isOnFeatureThread

        private class CountingScheduler(private val delegate: Scheduler) : Scheduler() {
            var interactionCount: Int = 0

            override fun createWorker(): Worker =
                delegate.createWorker().also { interactionCount++ }

            override fun start() {
                delegate.start()
            }

            override fun shutdown() {
                delegate.shutdown()
            }

            override fun scheduleDirect(run: Runnable): Disposable =
                delegate.scheduleDirect(run).also { interactionCount++ }

            override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable =
                delegate.scheduleDirect(run, delay, unit).also { interactionCount++ }

            override fun schedulePeriodicallyDirect(
                run: Runnable,
                initialDelay: Long,
                period: Long,
                unit: TimeUnit
            ): Disposable =
                delegate.schedulePeriodicallyDirect(run, initialDelay, period, unit)
                    .also { interactionCount++ }
        }
    }
}

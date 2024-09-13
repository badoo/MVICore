package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.element.WishToAction
import com.badoo.mvicore.utils.RxErrorRule
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests async functionality of [BaseAsyncFeature].
 */
@ExtendWith(RxErrorRule::class)
class AsyncBaseFeatureTest {

    private val featureExecutor = Executors.newSingleThreadExecutor { Thread(it, THREAD_FEATURE) }
    private val featureScheduler = Schedulers.from(featureExecutor)
    private val observationExecutor = Executors.newSingleThreadExecutor { Thread(it, THREAD_OBSERVATION) }
    private val observationScheduler = Schedulers.from(observationExecutor)
    private val disposable = CompositeDisposable()

    private lateinit var feature: AsyncFeature<Wish, State, News>

    @AfterEach
    fun after() {
        if (this::feature.isInitialized) {
            feature.dispose()
        }
        // Wait until feature is finished to avoid RejectedExecutionException
        Thread.sleep(100)
        // scheduler.shutdown() does not do anything
        featureExecutor.shutdown()
        observationExecutor.shutdown()
        disposable.clear()
    }

    @Test
    fun `allows creation with both schedulers`() {
        feature = testFeature(
            featureScheduler = Schedulers.trampoline(),
            observationScheduler = Schedulers.trampoline()
        )
    }

    @Test
    fun `executes bootstrapper on feature scheduler`() {
        val capture = ThreadCapture(THREAD_FEATURE)
        feature = testFeature(
            bootstrapper = {
                capture.capture()
                Observable.empty()
            }
        )
        capture.waitAndAssert()
    }

    @Test
    fun `executes bootsrappers action on feature scheduler`() {
        val capture = ThreadCapture(THREAD_FEATURE)
        feature = testFeature(
            actor = { _, _ ->
                capture.capture()
                Observable.empty()
            }
        )
        capture.waitAndAssert()
    }

    @Test
    fun `executes wish mapping on current thread scheduler`() {
        val capture = ThreadCapture(Thread.currentThread().name)
        feature = testFeature(
            bootstrapper = null,
            wishToAction = {
                capture.capture()
                Action()
            }
        )
        feature.accept(Wish())
        capture.waitAndAssert()
    }

    @Test
    fun `executes effects on feature scheduler`() {
        val capture = ThreadCapture(THREAD_FEATURE)
        feature = testFeature(
            reducer = { _, _ ->
                capture.capture()
                State()
            }
        )
        capture.waitAndAssert()
    }

    @Test
    fun `sends background news on feature scheduler`() {
        val capture = ThreadCapture(THREAD_FEATURE)
        feature = testFeature(bootstrapper = null)
        disposable += feature.backgroundNews.wrap().subscribe {
            capture.capture()
        }
        feature.accept(Wish())
        capture.waitAndAssert()
    }

    @Test
    fun `sends news on observation scheduler`() {
        val capture = ThreadCapture(THREAD_OBSERVATION)
        feature = testFeature(bootstrapper = null)
        disposable += feature.news.wrap().firstElement().subscribe {
            capture.capture()
        }
        feature.accept(Wish())
        capture.waitAndAssert()
    }

    @Test
    fun `sends initial state on observation scheduler`() {
        val capture = ThreadCapture(THREAD_OBSERVATION)
        feature = testFeature(bootstrapper = null)
        disposable += feature.wrap().firstElement().subscribe {
            capture.capture()
        }
        capture.waitAndAssert()
    }

    @Test
    fun `sends state updates on observation scheduler`() {
        val capture = ThreadCapture(THREAD_OBSERVATION)
        feature = testFeature(bootstrapper = null)
        disposable += feature.wrap().skip(1).firstElement().subscribe {
            capture.capture()
        }
        feature.accept(Wish())
        capture.waitAndAssert()
    }

    @Test
    fun `sends initial background state on current thread`() {
        val capture = ThreadCapture(Thread.currentThread().name)
        feature = testFeature(bootstrapper = null)
        disposable += feature.backgroundStates.wrap().firstElement().subscribe {
            capture.capture()
        }
        capture.waitAndAssert()
    }

    @Test
    fun `sends background state updates on feature scheduler`() {
        val capture = ThreadCapture(THREAD_FEATURE)
        feature = testFeature(bootstrapper = null)
        disposable += feature.backgroundStates.wrap().skip(1).firstElement().subscribe {
            capture.capture()
        }
        feature.accept(Wish())
        capture.waitAndAssert()
    }

    private fun testFeature(
        featureScheduler: Scheduler = this.featureScheduler,
        observationScheduler: Scheduler = this.observationScheduler,
        bootstrapper: Bootstrapper<Action>? = {
            Observable.just(Action()).observeOn(Schedulers.single())
        },
        wishToAction: WishToAction<Wish, Action> = { Action() },
        actor: Actor<State, Action, Effect> = { _, _ ->
            Observable.just(Effect()).observeOn(Schedulers.single())
        },
        reducer: Reducer<State, Effect> = { _, _ -> State() },
        postProcessor: PostProcessor<Action, Effect, State> = { _, _, _ -> null },
        newsPublisher: NewsPublisher<Action, Effect, State, News> = { _, _, _ -> News() }
    ) = BaseAsyncFeature(
        initialState = State(),
        bootstrapper = bootstrapper,
        wishToAction = wishToAction,
        actor = actor,
        reducer = reducer,
        newsPublisher = newsPublisher,
        postProcessor = postProcessor,
        schedulers = AsyncFeatureSchedulers(
            featureScheduler = featureScheduler,
            observationScheduler = observationScheduler
        )
    )

    private fun <T : Any> ObservableSource<T>.wrap() =
        Observable.wrap(this)

    class Wish

    class Action

    class Effect

    class State

    class News

    private class ThreadCapture(
        private val expected: String
    ) {
        private val countDownLatch = CountDownLatch(1)

        @Volatile
        private var actual: String? = null

        fun capture() {
            actual = Thread.currentThread().name
            countDownLatch.countDown()
        }

        fun waitAndAssert() {
            countDownLatch.await(10, TimeUnit.SECONDS)
            assertEquals(
                expected,
                actual,
                "Expected '$expected' but was executed on '$actual'"
            )
        }
    }

    companion object {
        private const val THREAD_FEATURE = "feature"
        private const val THREAD_OBSERVATION = "observation"
    }

}

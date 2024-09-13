package com.badoo.mvicore.android.lifecycle

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.Lifecycle
import com.badoo.binder.Binder
import com.badoo.binder.observeOn
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.internal.schedulers.RxThreadFactory
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.CountDownLatch
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

typealias LifecycleEvent = Lifecycle.(Binder.() -> Unit) -> Unit

class LifecycleExtensionsTest {
    private val subject = PublishSubject.create<Unit>()
    private val consumerTester = ConsumerTester()
    private val testLifecycleOwner = TestLifecycleOwner()

    private val mainScheduler = RxJavaPlugins
        .createSingleScheduler(RxThreadFactory("main", Thread.NORM_PRIORITY, false))
        .apply { start() }

    private val backgroundScheduler = RxJavaPlugins
        .createSingleScheduler(RxThreadFactory("background", Thread.NORM_PRIORITY, false))
        .apply { start() }

    @BeforeEach
    fun setup() {
        ArchTaskExecutor.getInstance()
            .setDelegate(object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
                override fun postToMainThread(runnable: Runnable) = runnable.run()
                override fun isMainThread(): Boolean = true
            })
    }

    @AfterEach
    fun teardown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @ParameterizedTest(name = "GIVEN lifecycle event {0} AND lifecycle states {1} THEN event consumption should be {2}")
    @MethodSource("generateTestData")
    fun `GIVEN lifecycle event AND lifecycle state THEN event consumption should be handled correctly`(
        lifecycleEvent: LifecycleEvent,
        lifecycleStates: List<Lifecycle.State>,
        eventShouldBeConsumed: Boolean
    ) {
        testLifecycleOwner.lifecycle.lifecycleEvent {
            bind(subject to consumerTester)
        }

        lifecycleStates.forEach { testLifecycleOwner.state = it }

        subject.onNext(Unit)

        if (eventShouldBeConsumed) {
            consumerTester.verifyInvoked()
        } else {
            consumerTester.verifyNotInvoked()
        }
        assertEquals(eventShouldBeConsumed, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND createDestroy with observe on schedulers dsl WHEN event emitted THEN verify correct thread called`() {
        val testThreadName = Thread.currentThread().name

        val subject = PublishSubject.create<Unit>()
        val mainThreadConsumerTester = ConsumerTester()
        val backgroundThreadConsumerTester = ConsumerTester()
        val unconfinedThreadConsumerTester = ConsumerTester()

        val countDownLatch = CountDownLatch(3)

        testLifecycleOwner.lifecycle.createDestroy {
            observeOn(mainScheduler) {
                bind(subject to Consumer {
                    mainThreadConsumerTester.accept(Unit)
                    countDownLatch.countDown()
                })
            }
            observeOn(backgroundScheduler) {
                bind(subject to Consumer {
                    backgroundThreadConsumerTester.accept(Unit)
                    countDownLatch.countDown()
                })
            }
            bind(subject to Consumer {
                unconfinedThreadConsumerTester.accept(Unit)
                countDownLatch.countDown()
            })
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED

        subject.onNext(Unit)

        countDownLatch.await()

        mainThreadConsumerTester.verifyThreadName("main")
        backgroundThreadConsumerTester.verifyThreadName("background")
        unconfinedThreadConsumerTester.verifyThreadName(testThreadName)
    }

    @Test
    fun `GIVEN initial lifecycle is created AND createDestroy with schedulers infix WHEN event emitted THEN verify correct thread called`() {
        val testThreadName = Thread.currentThread().name

        val subject = PublishSubject.create<Unit>()
        val mainThreadConsumerTester = ConsumerTester()
        val backgroundThreadConsumerTester = ConsumerTester()
        val unconfinedThreadConsumerTester = ConsumerTester()

        val countDownLatch = CountDownLatch(3)

        testLifecycleOwner.lifecycle.createDestroy {
            bind(subject to Consumer<Unit> {
                mainThreadConsumerTester.accept(Unit)
                countDownLatch.countDown()
            } observeOn mainScheduler)
            bind(subject to Consumer<Unit> {
                backgroundThreadConsumerTester.accept(Unit)
                countDownLatch.countDown()
            } observeOn backgroundScheduler)
            bind(subject to Consumer {
                unconfinedThreadConsumerTester.accept(Unit)
                countDownLatch.countDown()
            })
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED

        subject.onNext(Unit)

        countDownLatch.await()

        mainThreadConsumerTester.verifyThreadName("main")
        backgroundThreadConsumerTester.verifyThreadName("background")
        unconfinedThreadConsumerTester.verifyThreadName(testThreadName)
    }

    companion object {
        @JvmStatic
        @Suppress("LongMethod")
        fun generateTestData(): List<Arguments> {
            return listOf(
                TestData(
                    lifecycleEvent = Lifecycle::createDestroy,
                    lifecycleStates = emptyList(),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::createDestroy,
                    lifecycleStates = listOf(Lifecycle.State.CREATED),
                    eventShouldBeConsumed = true
                ),
                TestData(
                    lifecycleEvent = Lifecycle::createDestroy,
                    lifecycleStates = listOf(Lifecycle.State.STARTED),
                    eventShouldBeConsumed = true
                ),
                TestData(
                    lifecycleEvent = Lifecycle::createDestroy,
                    lifecycleStates = listOf(Lifecycle.State.RESUMED),
                    eventShouldBeConsumed = true
                ),
                TestData(
                    lifecycleEvent = Lifecycle::createDestroy,
                    lifecycleStates = listOf(
                        Lifecycle.State.CREATED,
                        Lifecycle.State.DESTROYED
                    ),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::startStop,
                    lifecycleStates = emptyList(),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::startStop,
                    lifecycleStates = listOf(Lifecycle.State.CREATED),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::startStop,
                    lifecycleStates = listOf(Lifecycle.State.STARTED),
                    eventShouldBeConsumed = true
                ),
                TestData(
                    lifecycleEvent = Lifecycle::startStop,
                    lifecycleStates = listOf(Lifecycle.State.RESUMED),
                    eventShouldBeConsumed = true
                ),
                TestData(
                    lifecycleEvent = Lifecycle::startStop,
                    lifecycleStates = listOf(
                        Lifecycle.State.CREATED,
                        Lifecycle.State.DESTROYED
                    ),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::resumePause,
                    lifecycleStates = emptyList(),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::resumePause,
                    lifecycleStates = listOf(Lifecycle.State.CREATED),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::resumePause,
                    lifecycleStates = listOf(Lifecycle.State.STARTED),
                    eventShouldBeConsumed = false
                ),
                TestData(
                    lifecycleEvent = Lifecycle::resumePause,
                    lifecycleStates = listOf(Lifecycle.State.RESUMED),
                    eventShouldBeConsumed = true
                ),
                TestData(
                    lifecycleEvent = Lifecycle::resumePause,
                    lifecycleStates = listOf(
                        Lifecycle.State.CREATED,
                        Lifecycle.State.DESTROYED
                    ),
                    eventShouldBeConsumed = false
                )
            ).map { Arguments.of(it.lifecycleEvent, it.lifecycleStates, it.eventShouldBeConsumed) }
        }
    }

    data class TestData(
        val lifecycleEvent: LifecycleEvent,
        val lifecycleStates: List<Lifecycle.State>,
        val eventShouldBeConsumed: Boolean
    )

    private class ConsumerTester : Consumer<Unit> {
        private var wasCalled: Boolean = false
        lateinit var threadName: String

        override fun accept(t: Unit) {
            wasCalled = true
            threadName = Thread.currentThread().name
        }

        fun verifyInvoked() {
            assertEquals(true, wasCalled)
        }

        fun verifyNotInvoked() {
            assertEquals(false, wasCalled)
        }

        fun verifyThreadName(name: String) {
            assertEquals(true, threadName.startsWith(name))
        }
    }
}

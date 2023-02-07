package com.badoo.mvicore.android.lifecycle

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.Lifecycle
import com.badoo.binder.observeOn
import io.reactivex.functions.Consumer
import io.reactivex.internal.schedulers.RxThreadFactory
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals

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

    @Before
    fun setup() {
        ArchTaskExecutor.getInstance()
            .setDelegate(object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
                override fun postToMainThread(runnable: Runnable) = runnable.run()
                override fun isMainThread(): Boolean = true
            })
    }

    @After
    fun teardown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @Test
    fun `GIVEN initial lifecycle not set AND createDestroy WHEN event emitted THEN consumer not invoked`() {
        testLifecycleOwner.lifecycle.createDestroy {
            bind(subject to consumerTester)
        }

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND createDestroy WHEN event emitted THEN consumer invoked AND observers exist`() {
        testLifecycleOwner.lifecycle.createDestroy {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED

        subject.onNext(Unit)

        consumerTester.verifyInvoked()
        assertEquals(true, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is started AND createDestroy WHEN event emitted THEN consumer invoked AND observers exist`() {
        testLifecycleOwner.lifecycle.createDestroy {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.STARTED

        subject.onNext(Unit)

        consumerTester.verifyInvoked()
        assertEquals(true, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is resumed AND createDestroy WHEN event emitted THEN consumer invoked AND observers exist`() {
        testLifecycleOwner.lifecycle.createDestroy {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.RESUMED

        subject.onNext(Unit)

        consumerTester.verifyInvoked()
        assertEquals(true, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND createDestroy AND lifecycle moved to destroyed WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.createDestroy {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED
        testLifecycleOwner.state = Lifecycle.State.DESTROYED

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle not set AND startStop WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.startStop {
            bind(subject to consumerTester)
        }

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND startStop WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.startStop {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is started AND startStop WHEN event emitted THEN consumer invoked AND observers exist`() {
        testLifecycleOwner.lifecycle.startStop {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.STARTED

        subject.onNext(Unit)

        consumerTester.verifyInvoked()
        assertEquals(true, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is resumed AND startStop WHEN event emitted THEN consumer invoked AND observers exist`() {
        testLifecycleOwner.lifecycle.startStop {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.RESUMED

        subject.onNext(Unit)

        consumerTester.verifyInvoked()
        assertEquals(true, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND startStop AND lifecycle moved to destroyed WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.startStop {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED
        testLifecycleOwner.state = Lifecycle.State.DESTROYED

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle not set AND resumePause WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.resumePause {
            bind(subject to consumerTester)
        }

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND resumePause WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.resumePause {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is started AND resumePause WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.resumePause {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.STARTED

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is resumed AND resumePause WHEN event emitted THEN consumer invoked AND observers exist`() {
        testLifecycleOwner.lifecycle.resumePause {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.RESUMED

        subject.onNext(Unit)

        consumerTester.verifyInvoked()
        assertEquals(true, subject.hasObservers())
    }

    @Test
    fun `GIVEN initial lifecycle is created AND resumePause AND lifecycle moved to destroyed WHEN event emitted THEN consumer not invoked AND no observers`() {
        testLifecycleOwner.lifecycle.resumePause {
            bind(subject to consumerTester)
        }
        testLifecycleOwner.state = Lifecycle.State.CREATED
        testLifecycleOwner.state = Lifecycle.State.DESTROYED

        subject.onNext(Unit)

        consumerTester.verifyNotInvoked()
        assertEquals(false, subject.hasObservers())
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

    private class ConsumerTester : Consumer<Unit> {
        private var wasCalled: Boolean = false
        lateinit var threadName: String

        override fun accept(t: Unit?) {
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

package com.badoo.mvicore.feature

import com.badoo.mvicore.TestHelper
import com.badoo.mvicore.scheduler.MVICoreSchedulers
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import kotlin.test.assertEquals
import com.badoo.mvicore.TestHelper.TestNews
import com.badoo.mvicore.TestHelper.TestState
import com.badoo.mvicore.TestHelper.TestWish
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject

class BaseFeatureSchedulerTest {

    private val actorScheduler = TestScheduler()
    private val alternateScheduler = MVICoreSchedulers.main

    private val alternateSchedulerThreadId =
        Single.fromCallable {
            Thread.currentThread().id.toString()
        }
            .subscribeOn(alternateScheduler)
            .blockingGet()

    private lateinit var resultCollector: AsyncResultCollector

    @Test
    fun `actor can specify alternative scheduler`() {
        val wishes = listOf<TestWish>(
            TestWish.OtherScheduler(alternateScheduler, 1),
            TestWish.OtherScheduler(alternateScheduler, 2)
        )
        val feature = createFeature(null, wishes.size)

        wishes.forEach { feature.accept(it) }

        val results = resultCollector.awaitAndGet()
        assertEquals(TestState(id = alternateSchedulerThreadId, counter = 1), results[0])
        assertEquals(TestState(id = alternateSchedulerThreadId, counter = 2), results[1])
    }

    @Test
    fun `action should be processed on action scheduler`() {
        val wishes = listOf<TestWish>(
                TestWish.ActionScheduler(1),
                TestWish.ActionScheduler(2),
                TestWish.ActionScheduler(3)
        )

        val feature = createFeature(alternateScheduler, wishes.size)

        wishes.forEach { feature.accept(it) }

        val results = resultCollector.awaitAndGet()
        assertEquals(TestState(id = alternateSchedulerThreadId, counter = 1), results[0])
        assertEquals(TestState(id = alternateSchedulerThreadId, counter = 2), results[1])
        assertEquals(TestState(id = alternateSchedulerThreadId, counter = 3), results[2])
    }

    private fun createFeature(
        actionScheduler: Scheduler?, resultCount: Int
    ): Feature<TestWish, TestState, TestNews> {
        val feature: Feature<TestWish, TestState, TestNews> = BaseFeature(
            initialState = TestState(),
            wishToAction = { wish -> wish },
            actor = TestHelper.TestActor(
                { _, _ ->  },
                actorScheduler
            ),
            reducer = TestHelper.TestReducer(),
            newsPublisher = TestHelper.TestNewsPublisher(),
            actionScheduler = actionScheduler
        )
        val subscription = PublishSubject.create<TestState>()
        feature.subscribe(subscription)
        resultCollector = AsyncResultCollector(subscription, resultCount)
        return feature
    }

    private class AsyncResultCollector(subscription: PublishSubject<TestState>, count: Int) {
        private val results = mutableListOf<TestState>()
        private val subject = SingleSubject.create<List<TestState>>()

        init {
            subscription.subscribe {
                results.add(it)
                if (results.size == count) {
                    subject.onSuccess(results)
                }
            }
        }

        fun awaitAndGet(): List<TestState> = subject.blockingGet()
    }
}
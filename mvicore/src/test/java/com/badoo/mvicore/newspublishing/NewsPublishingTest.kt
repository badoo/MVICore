package com.badoo.mvicore.newspublishing

import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
import com.badoo.mvicore.consumer.middleware.LoggingMiddleware
import com.badoo.mvicore.consumer.middlewareconfig.MiddlewareConfiguration
import com.badoo.mvicore.consumer.middlewareconfig.Middlewares
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.Always
import com.badoo.mvicore.consumer.middlewareconfig.WrappingCondition.InstanceOf
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.badoo.mvicore.feature.Feature
import com.badoo.mvicore.newspublishing.TestNews.News1
import com.badoo.mvicore.newspublishing.TestNews.News2
import com.badoo.mvicore.newspublishing.TestNews.News3
import com.badoo.mvicore.newspublishing.TestWish.Wish1
import com.badoo.mvicore.newspublishing.TestWish.Wish2
import com.badoo.mvicore.newspublishing.TestWish.Wish3
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.assertEquals

sealed class TestWish {
    object Wish1 : TestWish()
    object Wish2 : TestWish()
    object Wish3 : TestWish()
}

sealed class TestNews {
    object News1 : TestNews()
    object News2 : TestNews()
    object News3 : TestNews()
}

class Parameter(val middlewareConfiguration: MiddlewareConfiguration?) {
    override fun toString(): String = if (middlewareConfiguration != null) "with 3rd party middleware" else "without 3rd party middleware"
}

@RunWith(Parameterized::class)
class NewsPublishingTest(private val parameter: Parameter) {

    companion object {
        /**
         * The fact of using a wrapped news publisher or not shouldn't affect the news publishing logic.
         */
        @JvmStatic
        @Parameters(name = "{0}")
        fun parameters(): Iterable<Any?> = listOf<Any?>(
            // setup some middleware
            Parameter(
                MiddlewareConfiguration(
                    condition = Always,
                    factories = listOf(
                        { consumer -> LoggingMiddleware(consumer, {}) }
                    )
                )
            ),

            // not using middleware
            Parameter(null)
        )
    }

    private lateinit var feature: Feature<TestWish, Any, TestNews>
    private lateinit var newsTestSubscriber: TestObserver<TestNews>

    @Before
    fun setUp() {
        parameter.middlewareConfiguration?.let {
            Middlewares.configurations.add(it)
        }
    }

    @After
    fun tearDown() {
        Middlewares.configurations.clear()
    }

    @Test
    fun `created feature wo news publisher - emit wishes - no news produced`() {
        initializeFeatureWithNewsPublisher(null)

        listOf(Wish1, Wish2, Wish3).forEach(feature::accept)

        newsTestSubscriber.assertNoValues()
    }

    @Test
    fun `created feature with news publisher, null for everything - emit wishes - no news produced`() {
        initializeFeatureWithNewsPublisher { _, _, _ ->
            null
        }

        listOf(Wish1, Wish2, Wish3).forEach(feature::accept)

        newsTestSubscriber.assertNoValues()
    }

    @Test
    fun `created feature with news publisher, same for everything - emit N wishes - N same news produced`() {
        initializeFeatureWithNewsPublisher { _, _, _ ->
            News1
        }

        listOf(Wish1, Wish2, Wish3).forEach(feature::accept)

        newsTestSubscriber.assertValues(News1, News1, News1)
    }

    @Test
    fun `created feature with news publisher, different news - emit N wishes - N different news produced with a correct order`() {
        initializeFeatureWithNewsPublisher { action, _, _ ->
            when (action) {
                is Wish1 -> News1
                is Wish2 -> News2
                is Wish3 -> News3
                else -> null
            }
        }

        listOf(Wish3, Wish1, Wish2).forEach(feature::accept)

        newsTestSubscriber.assertValues(News3, News1, News2)
    }

    @Test
    fun `setup news publisher middleware, created feature with news publisher - emit N wishes - N events propagated to news publisher middleware`() {
        val testMiddleware = setupTestMiddlewareConfigurationForNews()

        initializeFeatureWithNewsPublisher { action, _, _ ->
            when (action) {
                is Wish1 -> News1
                is Wish2 -> News2
                is Wish3 -> News3
                else -> null
            }
        }

        listOf(Wish3, Wish1, Wish2).forEach(feature::accept)

        with(argumentCaptor<Triple<TestWish, Any, Any>>()) {
            verify(testMiddleware, times(3)).onElement(any(), capture())
            assertEquals(listOf(Wish3, Wish1, Wish2), allValues.map { it.first })
        }
    }

    private fun initializeFeatureWithNewsPublisher(newsPublisher: NewsPublisher<Any, Any, Any, TestNews>?) {
        feature = BaseFeature(
            initialState = mock(),
            bootstrapper = null,
            wishToAction = { it },
            actor = mock<Actor<Any, Any, Any>>().also {
                whenever(it.invoke(any(), any())).then { Observable.just(mock<Any>()) }
            },
            reducer = mock<Reducer<Any, Any>>().also {
                whenever(it.invoke(any(), any())).then { mock() }
            },
            postProcessor = null,
            newsPublisher = newsPublisher
        )
        newsTestSubscriber = Observable.wrap(feature.news).test()
    }

    @Suppress("RedundantLambdaArrow")
    private fun setupTestMiddlewareConfigurationForNews(): ConsumerMiddleware<Triple<TestWish, Any, Any>> {
        val testMiddleware = spy(LoggingMiddleware<Triple<TestWish, Any, Any>>(mock(), mock()))

        Middlewares.configurations.add(
            MiddlewareConfiguration(
                condition = InstanceOf(NewsPublisher::class.java),
                factories = listOf({ _ -> testMiddleware })
            )
        )

        return testMiddleware
    }
}

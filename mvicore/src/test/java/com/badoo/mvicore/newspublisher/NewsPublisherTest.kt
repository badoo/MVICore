package com.badoo.mvicore.newspublisher

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.badoo.mvicore.feature.Feature
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Test

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

class NewsPublisherTest {
    private lateinit var feature: Feature<TestWish, Any, TestNews>
    private lateinit var newsTestSubscriber: TestObserver<TestNews>

    @Test
    fun `created feature wo news publisher - emmit wishes - no news produced`() {
        initializeFeatureWithNewsPublisher { null }

        listOf(TestWish.Wish1, TestWish.Wish2, TestWish.Wish3).forEach(feature::accept)

        newsTestSubscriber.assertNoValues()
    }

    @Test
    fun `created feature with news publisher, null for everything - emmit wishes - no news produced`() {
        initializeFeatureWithNewsPublisher {
            object : NewsPublisher<Any, Any, Any, TestNews> {
                override fun invoke(action: Any, effect: Any, state: Any): TestNews? = null
            }
        }

        listOf(TestWish.Wish1, TestWish.Wish2, TestWish.Wish3).forEach(feature::accept)

        newsTestSubscriber.assertNoValues()
    }

    @Test
    fun `created feature with news publisher, same for everything - emmit N wishes - N same news produced`() {
        initializeFeatureWithNewsPublisher {
            object : NewsPublisher<Any, Any, Any, TestNews> {
                override fun invoke(action: Any, effect: Any, state: Any): TestNews? = TestNews.News1
            }
        }

        listOf(TestWish.Wish1, TestWish.Wish2, TestWish.Wish3).forEach(feature::accept)

        newsTestSubscriber.assertValues(TestNews.News1, TestNews.News1, TestNews.News1)
    }

    @Test
    fun `created feature with news publisher, different news - emmit N wishes - N different news produced with a correct order`() {
        initializeFeatureWithNewsPublisher {
            object : NewsPublisher<Any, Any, Any, TestNews> {
                override fun invoke(action: Any, effect: Any, state: Any): TestNews? {
                    return when (action) {
                        is TestWish.Wish1 -> TestNews.News1
                        is TestWish.Wish2 -> TestNews.News2
                        is TestWish.Wish3 -> TestNews.News3
                        else -> null
                    }
                }
            }
        }

        listOf(TestWish.Wish3, TestWish.Wish1, TestWish.Wish2).forEach(feature::accept)

        newsTestSubscriber.assertValues(TestNews.News3, TestNews.News1, TestNews.News2)
    }

    private fun initializeFeatureWithNewsPublisher(newsPublisherCreator: () -> NewsPublisher<Any, Any, Any, TestNews>?) {
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
            newsPublisher = newsPublisherCreator()
        )
        newsTestSubscriber = Observable.wrap(feature.news).test()
    }
}

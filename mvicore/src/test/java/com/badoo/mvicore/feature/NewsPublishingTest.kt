package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.TestNews.News1
import com.badoo.mvicore.feature.TestNews.News2
import com.badoo.mvicore.feature.TestNews.News3
import com.badoo.mvicore.feature.TestWish.Wish1
import com.badoo.mvicore.feature.TestWish.Wish2
import com.badoo.mvicore.feature.TestWish.Wish3
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

class NewsPublishingTest {
    private lateinit var feature: Feature<TestWish, Any, TestNews>
    private lateinit var newsTestSubscriber: TestObserver<TestNews>

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
}

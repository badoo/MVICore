package com.badoo.mvicore.newspublishing

import com.badoo.binder.middleware.base.Middleware
import com.badoo.binder.middleware.config.MiddlewareConfiguration
import com.badoo.binder.middleware.config.Middlewares
import com.badoo.binder.middleware.config.WrappingCondition
import com.badoo.binder.middleware.config.WrappingCondition.InstanceOf
import com.badoo.mvicore.consumer.middleware.ConsumerMiddleware
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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.observers.TestObserver
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever


sealed class TestWish {
    data object Wish1 : TestWish()
    data object Wish2 : TestWish()
    data object Wish3 : TestWish()
}

sealed class TestNews {
    data object News1 : TestNews()
    data object News2 : TestNews()
    data object News3 : TestNews()
}

class Parameter(val middlewareConfiguration: MiddlewareConfiguration?) {
    override fun toString(): String =
        if (middlewareConfiguration != null) "with 3rd party middleware" else "without 3rd party middleware"
}

private fun <T : Any> createMiddlewareStub(consumer: Consumer<T>): Middleware<Any, T> =
    object : Middleware<Any, T>(consumer) {}

class ConfigurationArgumentProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments?> {
        return Stream.of(
            Parameter(null),
            Parameter(
                MiddlewareConfiguration(condition = WrappingCondition.Always,
                    factories = listOf { consumer -> createMiddlewareStub(consumer) })
            )
        ).map(Arguments::of)
    }
}

class NewsPublishingTest {

    private lateinit var feature: Feature<TestWish, Any, TestNews>
    private lateinit var newsTestSubscriber: TestObserver<TestNews>

    private fun before(configuration: MiddlewareConfiguration?) {
        configuration?.let {
            Middlewares.configurations.add(it)
        }
    }

    @AfterEach
    fun tearDown() {
        Middlewares.configurations.clear()
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigurationArgumentProvider::class)
    fun `feature wo news publisher - emit wishes - no news produced`(parameter: Parameter) {
        before(parameter.middlewareConfiguration)
        initializeFeatureWithNewsPublisher(null)

        listOf(Wish1, Wish2, Wish3).forEach(feature::accept)

        newsTestSubscriber.assertNoValues()
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigurationArgumentProvider::class)
    fun `feature with news publisher which returns null - emit wishes - no news produced`(
        parameter: Parameter
    ) {
        before(parameter.middlewareConfiguration)
        initializeFeatureWithNewsPublisher { _, _, _ ->
            null
        }

        listOf(Wish1, Wish2, Wish3).forEach(feature::accept)

        newsTestSubscriber.assertNoValues()
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigurationArgumentProvider::class)
    fun `feature with news publisher which returns 1 news - emit N wishes - N same news produced`(
        parameter: Parameter
    ) {
        before(parameter.middlewareConfiguration)
        initializeFeatureWithNewsPublisher { _, _, _ ->
            News1
        }

        listOf(Wish1, Wish2, Wish3).forEach(feature::accept)

        newsTestSubscriber.assertValues(News1, News1, News1)
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigurationArgumentProvider::class)
    fun `feature with news publisher which returns different news - emit N wishes - N different news produced with a correct order`(
        parameter: Parameter
    ) {
        before(parameter.middlewareConfiguration)
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

    @ParameterizedTest
    @ArgumentsSource(ConfigurationArgumentProvider::class)
    fun `news publisher middleware, feature with news publisher - emit N wishes - N events propagated to news publisher middleware`(
        parameter: Parameter
    ) {
        before(parameter.middlewareConfiguration)
        setupTestMiddlewareConfigurationForNews()

        initializeFeatureWithNewsPublisher { action, _, _ ->
            when (action) {
                is Wish1 -> News1
                is Wish2 -> News2
                is Wish3 -> News3
                else -> null
            }
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
        val testMiddleware = spy(createMiddlewareStub(mock<Consumer<Triple<TestWish, Any, Any>>>()))

        Middlewares.configurations.add(
            MiddlewareConfiguration(
                condition = InstanceOf(NewsPublisher::class.java),
                factories = listOf { _ -> testMiddleware }
            )
        )

        return testMiddleware
    }
}

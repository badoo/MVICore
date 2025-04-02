package com.badoo.mvicore.bootstrapper

import com.badoo.binder.middleware.base.Middleware
import com.badoo.binder.middleware.config.MiddlewareConfiguration
import com.badoo.binder.middleware.config.Middlewares
import com.badoo.binder.middleware.config.WrappingCondition
import com.badoo.mvicore.bootstrapper.BootstrapperTest.Action.Action1
import com.badoo.mvicore.bootstrapper.BootstrapperTest.Action.Action2
import com.badoo.mvicore.bootstrapper.BootstrapperTest.Action.Action3
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.feature.BaseFeature
import com.badoo.mvicore.feature.Feature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.subjects.ReplaySubject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class BootstrapperTest {

    private sealed class Action {
        data object Action1 : Action()
        data object Action2 : Action()
        data object Action3 : Action()
    }

    private lateinit var feature: Feature<Any, Any, Any>
    private lateinit var actionHandler: TestObserver<Action>

    @AfterEach
    fun tearDown() {
        Middlewares.configurations.clear()
    }

    @Test
    fun `GIVEN Feature without Bootstrapper THEN Bootstrapper doesn't emit actions`() {
        initializeFeatureWithBootstrapper(null)

        actionHandler.assertEmpty()
    }

    @Test
    fun `GIVEN Feature with Bootstrapper WHEN Bootstrapper doesn't emit actions THEN no actions caught`() {
        val bootstrapper: Bootstrapper<Action> = {
            Observable.empty()
        }
        initializeFeatureWithBootstrapper(bootstrapper)

        actionHandler.assertEmpty()
    }

    @Test
    fun `GIVEN Feature with Bootstrapper THEN Bootstrapper executes only ones`() {
        var counter = 0
        val bootstrapper = {
            counter++
            Observable.empty<Action>()
        }
        initializeFeatureWithBootstrapper(bootstrapper)

        listOf(1, 2, 3).forEach(feature::accept)

        assertEquals(counter, 1)
    }

    @Test
    fun `GIVEN Feature with Bootstrapper WHEN Bootstrapper emits actions THEN the order of actions remains unchanged`() {
        val bootstrapper = {
            Observable.just(Action1, Action2, Action1, Action3)
        }
        initializeFeatureWithBootstrapper(bootstrapper)

        actionHandler.assertValues(Action1, Action2, Action1, Action3)
    }

    @Test
    fun `GIVEN Feature with Bootstrapper WHEN Bootstrapper emits actions THEN they propagates to Bootstrapper Middleware`() {
        val testMiddleware = setupTestMiddlewareConfiguration()
        val bootstrapper = {
            Observable.just(Action2, Action3, Action1)
        }
        initializeFeatureWithBootstrapper(bootstrapper)

        with(argumentCaptor<Action>()) {
            verify(testMiddleware, times(3)).onElement(any(), capture())
            assertEquals(listOf(Action2, Action3, Action1), allValues)
        }
    }

    private fun setupTestMiddlewareConfiguration(): Middleware<Any, Action> {
        val middlewareStub = spy(object : Middleware<Any, Action>(mock()) {})

        Middlewares.configurations.add(
            MiddlewareConfiguration(
                condition = WrappingCondition.Always,
                factories = listOf { middlewareStub }
            )
        )

        return middlewareStub
    }

    private fun initializeFeatureWithBootstrapper(bootstrapper: Bootstrapper<Action>?) {
        val actions = ReplaySubject.create<Action>()
        feature = BaseFeature<Any, Action, Any, Any, Any>(
            initialState = Any(),
            bootstrapper = bootstrapper,
            wishToAction = { Action1 },
            actor = { _, action ->
                actions.onNext(action)
                Observable.empty()
            },
            reducer = { _, _ -> Any() },
            postProcessor = null,
            newsPublisher = null
        )

        actionHandler = actions.test()
    }
}

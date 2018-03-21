@file:Suppress("IllegalIdentifier")

package com.badoo.mvicore2

import com.badoo.mvicore2.Demo.DemoActor
import com.badoo.mvicore2.Demo.DemoEvent
import com.badoo.mvicore2.Demo.DemoEvent.Type.ADD
import com.badoo.mvicore2.Demo.DemoEvent.Type.MULTIPLY
import com.badoo.mvicore2.Demo.DemoEventTransformer
import com.badoo.mvicore2.Demo.DemoState
import com.badoo.mvicore2.Demo.DemoStateTransformer
import com.badoo.mvicore2.Demo.DemoViewModel
import com.badoo.mvicore2.Demo.DemoViewModel.Loading
import com.badoo.mvicore2.Demo.DemoViewModel.Value
import com.badoo.mvicore2.Demo.DemoWish
import com.badoo.mvicore2.binder.Bindable
import com.badoo.mvicore2.binder.Binder
import com.badoo.mvicore2.binder.TestBindable
import com.badoo.mvicore2.lifecycle.Lifecycle
import com.badoo.mvicore2.lifecycle.TestLifecycle
import com.badoo.mvicore2.store.Store
import com.badoo.mvicore2.store.Store.Companion.create
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val INITIAL_VALUE = 1

class DemoTest {

    private val eventTransformer = DemoEventTransformer
    private val stateTransformer = DemoStateTransformer
    private val lifecycle: TestLifecycle = Lifecycle.test()
    private val view: TestBindable<DemoViewModel, DemoEvent> = Bindable.test()
    private val store: Store<DemoWish, DemoState> = create(DemoState(value = INITIAL_VALUE), DemoActor())
    private val newsSource: PublishSubject<Unit> = PublishSubject.create()
    private val newTransformer = Demo.NewsTransformer
    private val binder = Binder.from(lifecycle)

    @Before
    fun setUp() {
        binder.twoWay(eventTransformer, stateTransformer).bind(view to store)
        binder.oneWay(newTransformer).bind(newsSource to store)
    }

    @After
    fun tearDown() = lifecycle.stop()

    @Test
    fun `when started observe no values`() {
        lifecycle.start()

        view.observer.assertValues(Value(INITIAL_VALUE))
    }

    @Test
    fun `on add when started observe loading and changed value`() {
        val increment = 5
        val expectedFinalValue = INITIAL_VALUE + increment

        lifecycle.start()

        view.output.onNext(DemoEvent(increment, ADD))

        view.observer.assertValues(
                Value(INITIAL_VALUE),
                Loading,
                Value(expectedFinalValue)
        )
    }

    @Test
    fun `on multiply when started observe loading and changed value`() {
        val factor = 15
        val expectedFinalValue = INITIAL_VALUE * factor

        lifecycle.start()

        view.output.onNext(DemoEvent(factor, MULTIPLY))

        view.observer.assertValues(
                Value(INITIAL_VALUE),
                Loading,
                Value(expectedFinalValue)
        )
    }

    @Test
    fun `when not started events are ignored`() {
        val factor = 15

        // lifecycle: not started

        view.output.onNext(DemoEvent(factor, MULTIPLY))

        view.observer.assertNoValues()
    }

    @Test
    fun `when not started news are ignored`() {

        // lifecycle: not started

        newsSource.onNext(Unit)

        view.observer.assertNoValues()
    }

    @Test
    fun `on news when started observe loading and changed value`() {
        val increment = 1
        val expectedFinalValue = INITIAL_VALUE + increment

        lifecycle.start()

        newsSource.onNext(Unit)

        view.observer.assertValues(
                Value(INITIAL_VALUE),
                Loading,
                Value(expectedFinalValue)
        )
    }

}

package com.badoo.mvicore.boundary

import com.badoo.mvicore.TestHelper.Companion.initialCounter
import com.badoo.mvicore.TestHelper.TestUiEvent
import com.badoo.mvicore.TestHelper.TestUiEvent.*
import com.badoo.mvicore.TestHelper.TestViewModel
import com.badoo.mvicore.onNextEvents
import com.badoo.mvicore.overrideAssertsForTesting
import io.reactivex.observers.TestObserver
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

abstract class BoundaryTest(
    val boundary: Boundary<TestUiEvent, TestViewModel>
) {
    lateinit var viewModels: TestObserver<TestViewModel>

    @Before
    fun prepare() {
        MockitoAnnotations.initMocks(this)
        overrideAssertsForTesting(false)

        viewModels = boundary.viewModels.test()
    }

    @After
    fun tearDown() {
        boundary.dispose()
    }

    @Test
    fun `if there are no ui events, boundary only emits initial viewmodel`() {
        assertEquals(1, viewModels.onNextEvents().size)
    }

    @Test
    fun `emitted initial viewmodel is correct`() {
        val viewModel: TestViewModel = viewModels.onNextEvents().first() as TestViewModel
        assertEquals(initialCounter, viewModel.counter)
    }

    @Test
    fun `ui events trigger viewmodel emissions`() {
        val uiEvents = listOf(
            ImportantButtonClicked,
            SpinnerValueChanged(3)
        )

        uiEvents.forEach { boundary.onUiEvent(it) }

        assertEquals(1 + uiEvents.size, viewModels.onNextEvents().size)
    }
}

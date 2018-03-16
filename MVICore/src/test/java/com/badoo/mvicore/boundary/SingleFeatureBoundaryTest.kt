package com.badoo.mvicore.boundary

import com.badoo.mvicore.TestHelper
import com.badoo.mvicore.TestHelper.Companion.instantFulfillAmount1
import com.badoo.mvicore.TestHelper.TestFeature
import com.badoo.mvicore.TestHelper.TestState
import com.badoo.mvicore.TestHelper.TestUiEvent
import com.badoo.mvicore.TestHelper.TestViewModel
import com.badoo.mvicore.TestHelper.TestWish
import com.badoo.mvicore.core.DefaultEngine
import com.badoo.mvicore.onNextEvents
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert
import org.junit.Test

class SingleFeatureBoundaryTest : BoundaryTest(boundary = TestBoundary()) {

    class TestBoundary : SingleFeatureBoundary<TestUiEvent, TestWish, TestState, TestViewModel>(
        feature = TestFeature(DefaultEngine(), PublishSubject.create(), PublishSubject.create()),
        uiEventMapper = {
            when (it) {
                is TestUiEvent.ImportantButtonClicked -> TestWish.IncreasCounterBy(instantFulfillAmount1)
                is TestUiEvent.SpinnerValueChanged -> TestWish.IncreasCounterBy(it.value)
            }
        },
        viewModelTransformer = {
            TestViewModel(
                counter = it.counter
            )
        }
    )

    @Test
    fun `state of viewmodels emitted after for ui events matches expectations`() {
        val spinnerValue = 3
        val uiEvents = listOf(
            TestUiEvent.ImportantButtonClicked,
            TestUiEvent.SpinnerValueChanged(spinnerValue)
        )

        uiEvents.forEach { boundary.onUiEvent(it) }

        val viewModel: TestViewModel = viewModels.onNextEvents().last() as TestViewModel
        Assert.assertEquals(TestHelper.initialCounter + instantFulfillAmount1 + spinnerValue, viewModel.counter)
    }
}

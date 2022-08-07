package com.bumble.order

import com.badoo.binder.lifecycle.ManualLifecycle
import com.bumble.order.component.Analytics
import com.bumble.order.component.View
import org.junit.Ignore
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class BindingOrderDisposeTest {

    @Test
    @Ignore
    fun `missing events on dispose`() {
        val lifecycle = ManualLifecycle()
        val view = spy(View())
        val analytics = spy(Analytics())
        val componentBinder =
            ComponentBinder(view = view, analytics = analytics, lifecycle = lifecycle)
        componentBinder.bindFeatureFirst()

        lifecycle.begin()
        repeat(SCORE_LIMIT) { view.score() }

        verify(view, times(SCORE_LIMIT + 1)).accept(any()) //+1 initial feature state
        verify(analytics, times(SCORE_LIMIT)).accept(any())
    }

    private companion object {
        const val SCORE_LIMIT = 5
    }
}
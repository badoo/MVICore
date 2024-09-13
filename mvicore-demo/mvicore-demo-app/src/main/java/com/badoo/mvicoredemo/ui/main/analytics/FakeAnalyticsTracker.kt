package com.badoo.mvicoredemo.ui.main.analytics

import android.content.Context
import android.widget.Toast
import com.badoo.mvicoredemo.ui.main.event.UiEvent
import io.reactivex.rxjava3.functions.Consumer

class FakeAnalyticsTracker(
    private val context: Context
) : Consumer<UiEvent> {

    var showToasts: Boolean = false

    override fun accept(uiEvent: UiEvent) {
        if (showToasts) {
            Toast.makeText(
                context,
                "Analytics: ${uiEvent.javaClass.simpleName}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


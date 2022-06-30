package com.bumble.order

import com.badoo.binder.Binder
import com.badoo.binder.lifecycle.ManualLifecycle
import com.badoo.binder.using
import com.bumble.order.component.Analytics
import com.bumble.order.component.Feature
import com.bumble.order.component.NewsConsumer
import com.bumble.order.component.StateToViewModel
import com.bumble.order.component.View
import com.bumble.order.component.ViewEventToAnalyticsEvent
import com.bumble.order.component.ViewEventToWish

class ComponentBinder(
    private val view: View = View(),
    private val feature: Feature = Feature(),
    private val analytics: Analytics = Analytics(),
    lifecycle: ManualLifecycle = ManualLifecycle(),
    private val newsConsumer: NewsConsumer = NewsConsumer(lifecycle)
) {
    private val binder = Binder(lifecycle)

    //Works like a charm
    fun bindAnalyticsFirst() {
        binder.bind(view to analytics using ViewEventToAnalyticsEvent)
        binder.bind(view to feature using ViewEventToWish)
        binder.bind(feature to view using StateToViewModel)
        binder.bind(feature.news to newsConsumer)
    }

    //Analytics class does not get the last event from the view class
    fun bindFeatureFirst() {
        binder.bind(view to feature using ViewEventToWish)
        binder.bind(view to analytics using ViewEventToAnalyticsEvent)
        binder.bind(feature to view using StateToViewModel)
        binder.bind(feature.news to newsConsumer)
    }
}
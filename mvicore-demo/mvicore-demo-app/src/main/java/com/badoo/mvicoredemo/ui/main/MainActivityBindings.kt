package com.badoo.mvicoredemo.ui.main

import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicore.android.AndroidBindings
import com.badoo.mvicore.binder.named
import com.badoo.mvicore.binder.using
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.event.UiEventTransformer1
import com.badoo.mvicoredemo.ui.main.event.UiEventTransformer2
import com.badoo.mvicoredemo.ui.main.news.NewsListener
import com.badoo.mvicoredemo.ui.main.viewmodel.ViewModelTransformer
import com.badoo.mvicoredemo.utils.combineLatest

class MainActivityBindings(
    view: MainActivity,
    private val feature1: Feature1,
    private val feature2: Feature2,
    private val analyticsTracker: FakeAnalyticsTracker,
    private val newsListener: NewsListener
) : AndroidBindings<MainActivity>(view) {

    override fun setup(view: MainActivity) {
        binder.bind(combineLatest(feature1, feature2) to view using ViewModelTransformer() named "MainActivity.ViewModels")
        binder.bind(view to feature1 using UiEventTransformer1())
        binder.bind(view to feature2 using UiEventTransformer2())
        binder.bind(view to analyticsTracker named "MainActivity.Analytics")
        binder.bind(feature2.news to newsListener named "MainActivity.News")
    }
}

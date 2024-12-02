package com.badoo.mvicoredemo.ui.main

import com.badoo.binder.named
import com.badoo.binder.using
import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicore.android.lifecycle.BinderController
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.event.UiEventTransformer1
import com.badoo.mvicoredemo.ui.main.event.UiEventTransformer2
import com.badoo.mvicoredemo.ui.main.news.NewsListener
import com.badoo.mvicoredemo.ui.main.viewmodel.ViewModelTransformer
import com.badoo.mvicoredemo.utils.combineLatest

class MainActivityBindings(
    private val feature1: Feature1,
    private val feature2: Feature2,
    private val analyticsTracker: FakeAnalyticsTracker,
    private val newsListener: NewsListener
) {

    fun setup(view: MainActivity) {
        val binderController = BinderController()
        binderController.createDestroy(view.lifecycle) {
            bind(
                combineLatest(
                    feature1,
                    feature2
                ) to view using ViewModelTransformer() named "MainActivity.ViewModels"
            )
            bind(view to feature1 using UiEventTransformer1())
            bind(view to feature2 using UiEventTransformer2())
            bind(view to analyticsTracker named "MainActivity.Analytics")
            bind(feature2.news to newsListener named "MainActivity.News")
        }
        binderController.drain()
    }
}

package com.badoo.mvicoredemo.ui.main.di.module

import android.content.Context
import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicoredemo.di.activityscope.scope.ActivityScope
import com.badoo.mvicoredemo.ui.main.MainActivity
import com.badoo.mvicoredemo.ui.main.MainActivityBindings
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.news.NewsListener
import dagger.Module
import dagger.Provides

@Module
class MainScreenModule(
    private val mainActivity: MainActivity
) {

    @Provides
    fun mainActivity() =
        mainActivity

    @Provides
    @ActivityScope
    fun bindings(
        view: MainActivity,
        feature1: Feature1,
        feature2: Feature2,
        analyticsTracker: FakeAnalyticsTracker,
        newsListener: NewsListener
    ): MainActivityBindings =
        MainActivityBindings(
            view = view,
            feature1 = feature1,
            feature2 = feature2,
            analyticsTracker = analyticsTracker,
            newsListener = newsListener
        )

    @Provides
    @ActivityScope
    fun analyticsTracker(context: Context) =
        FakeAnalyticsTracker(context)

    @Provides
    @ActivityScope
    fun newsListener(context: Context) =
        NewsListener(context)
}

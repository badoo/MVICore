package com.badoo.mvicoredemo.ui.main.di.module

import android.app.Application
import com.badoo.mvicoredemo.di.usersessionscope.component.UserComponentEntryPoint
import com.badoo.mvicoredemo.ui.main.MainActivityBindings
import com.badoo.mvicoredemo.ui.main.analytics.FakeAnalyticsTracker
import com.badoo.mvicoredemo.ui.main.news.NewsListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
class MainScreenModule {

    @Provides
    @ActivityScoped
    fun bindings(
        application: Application,
        analyticsTracker: FakeAnalyticsTracker,
        newsListener: NewsListener
    ): MainActivityBindings {
        val userPartsEntryPoint = UserComponentEntryPoint.get(application)

        return MainActivityBindings(
            feature1 = userPartsEntryPoint.feature1(),
            feature2 = userPartsEntryPoint.feature2(),
            analyticsTracker = analyticsTracker,
            newsListener = newsListener
        )
    }

    @Provides
    @ActivityScoped
    fun analyticsTracker(application: Application) =
        FakeAnalyticsTracker(application)

    @Provides
    @ActivityScoped
    fun newsListener(application: Application) =
        NewsListener(application)
}

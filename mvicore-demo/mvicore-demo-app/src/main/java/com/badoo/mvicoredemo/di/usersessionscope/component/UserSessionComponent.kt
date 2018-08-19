package com.badoo.mvicoredemo.di.usersessionscope.component

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware
import com.badoo.mvicore.debugdrawer.MviCoreControlsModule
import com.badoo.mvicoredemo.App
import com.badoo.mvicoredemo.di.appscope.component.AppComponent
import com.badoo.mvicoredemo.di.usersessionscope.module.FeatureModule
import com.badoo.mvicoredemo.di.usersessionscope.scope.UserSessionScope
import dagger.Component


@UserSessionScope
@Component(
    dependencies = [
        AppComponent::class
    ],
    modules = [
        FeatureModule::class
    ]
)
interface UserSessionComponent {
    // expose AppComponent
    fun provideApp(): App
    fun provideContext(): Context
    fun provideResources(): Resources
    fun provideSharedPreferences(): SharedPreferences
    fun recordStore(): PlaybackMiddleware.RecordStore
    fun debugDrawerControls(): MviCoreControlsModule

    // expose FeatureModule
    fun feature1(): Feature1
    fun feature2(): Feature2
}

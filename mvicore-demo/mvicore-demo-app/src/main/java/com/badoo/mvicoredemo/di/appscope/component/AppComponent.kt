package com.badoo.mvicoredemo.di.appscope.component

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore
import com.badoo.mvicore.debugdrawer.MviCoreControlsModule
import com.badoo.mvicoredemo.App
import com.badoo.mvicoredemo.di.appscope.module.AndroidModule
import com.badoo.mvicoredemo.di.appscope.module.MviCoreModule
import com.badoo.mvicoredemo.di.appscope.scope.AppScope
import dagger.Component


@AppScope
@Component(
    modules = [
        AndroidModule::class,
        MviCoreModule::class
    ]
)
interface AppComponent {
    // expose AndroidModule
    fun provideApp(): App
    fun provideContext(): Context
    fun provideResources(): Resources
    fun provideSharedPreferences(): SharedPreferences

    // expose MviCoreModule
//    fun logger(): Logger
    fun recordStore(): RecordStore
    fun debugDrawerControls(): MviCoreControlsModule

    fun inject(app: App)
}

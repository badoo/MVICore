package com.badoo.mvicoredemo.ui.main.di.component

import com.badoo.mvicoredemo.di.activityscope.module.ActivityModule
import com.badoo.mvicoredemo.di.activityscope.scope.ActivityScope
import com.badoo.mvicoredemo.di.usersessionscope.component.UserSessionComponent
import com.badoo.mvicoredemo.ui.main.MainActivity
import com.badoo.mvicoredemo.ui.main.di.module.MainScreenModule
import dagger.Component

@ActivityScope
@Component(
    dependencies = [
        UserSessionComponent::class
    ],
    modules = [
        ActivityModule::class,
        MainScreenModule::class
    ]
)
interface MainScreenComponent {
    fun inject(activity: MainActivity)
}

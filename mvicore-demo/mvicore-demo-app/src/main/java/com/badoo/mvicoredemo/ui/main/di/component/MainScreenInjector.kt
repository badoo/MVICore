package com.badoo.mvicoredemo.ui.main.di.component

import com.badoo.mvicoredemo.di.activityscope.module.ActivityModule
import com.badoo.mvicoredemo.di.usersessionscope.component.UserSessionScopedComponent
import com.badoo.mvicoredemo.ui.main.MainActivity
import com.badoo.mvicoredemo.ui.main.di.module.MainScreenModule

object MainScreenInjector {

    fun get(activity: MainActivity): MainScreenComponent =
        DaggerMainScreenComponent.builder()
            .userSessionComponent(UserSessionScopedComponent.get())
            .activityModule(ActivityModule(activity))
            .mainScreenModule(MainScreenModule(activity))
            .build()
}

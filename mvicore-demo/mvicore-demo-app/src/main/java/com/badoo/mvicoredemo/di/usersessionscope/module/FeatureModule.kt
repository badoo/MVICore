package com.badoo.mvicoredemo.di.usersessionscope.module

import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicoredemo.di.usersessionscope.component.UserSessionComponent
import com.badoo.mvicoredemo.di.usersessionscope.component.UserSessionScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(UserSessionComponent::class)
class FeatureModule {
    @Provides
    @UserSessionScope
    fun feature1(): Feature1 =
        Feature1()

    @Provides
    @UserSessionScope
    fun feature2(): Feature2 =
        Feature2()
}

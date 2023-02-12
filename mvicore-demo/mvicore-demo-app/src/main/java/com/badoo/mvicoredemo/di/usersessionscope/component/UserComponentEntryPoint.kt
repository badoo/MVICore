package com.badoo.mvicoredemo.di.usersessionscope.component

import android.app.Application
import com.badoo.feature1.Feature1
import com.badoo.feature2.Feature2
import com.badoo.mvicoredemo.di.usersessionscope.UserManager
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn

@InstallIn(UserSessionComponent::class)
@EntryPoint
interface UserComponentEntryPoint {
    fun feature1(): Feature1
    fun feature2(): Feature2

    companion object {
        fun get(application: Application): UserComponentEntryPoint =
            get(UserManager.getUserManager(application).userComponent!!)

        fun get(userComponent: UserSessionComponent): UserComponentEntryPoint =
            EntryPoints.get(userComponent, UserComponentEntryPoint::class.java)
    }
}

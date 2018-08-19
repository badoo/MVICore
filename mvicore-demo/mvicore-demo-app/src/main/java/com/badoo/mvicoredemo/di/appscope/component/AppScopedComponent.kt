package com.badoo.mvicoredemo.di.appscope.component

import android.content.Context
import com.badoo.mvicoredemo.di.ScopedComponent
import com.badoo.mvicoredemo.di.appscope.module.AndroidModule

class AppScopedComponent(
    private val context: Context
) : ScopedComponent<AppComponent>() {

    override fun create(): AppComponent =
        DaggerAppComponent.builder()
            .androidModule(AndroidModule(context))
            .build()
}


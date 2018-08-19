package com.badoo.mvicoredemo.di.appscope.module

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources

import com.badoo.mvicoredemo.App

import dagger.Module
import dagger.Provides

@Module
class AndroidModule(context: Context) {
    private val context: Context = context.applicationContext

    @Provides
    fun provideApp(): App {
        return context as App
    }

    @Provides
    fun provideContext(): Context {
        return context
    }

    @Provides
    fun provideResources(): Resources {
        return context.resources
    }

    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(App::class.java.name, Context.MODE_PRIVATE)
    }
}

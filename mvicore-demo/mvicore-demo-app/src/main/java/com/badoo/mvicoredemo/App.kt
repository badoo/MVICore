package com.badoo.mvicoredemo

import android.app.Application
import com.badoo.binder.middleware.config.MiddlewareConfiguration
import com.badoo.binder.middleware.config.Middlewares
import com.badoo.binder.middleware.config.WrappingCondition
import com.badoo.mvicore.consumer.middleware.LoggingMiddleware
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore
import com.badoo.mvicore.middleware.DefaultPluginStore
import com.badoo.mvicore.middleware.IdeaPluginMiddleware
import dagger.hilt.android.HiltAndroidApp
import io.palaima.debugdrawer.timber.data.LumberYard
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var recordStore: RecordStore

    private val defaultStore = DefaultPluginStore(BuildConfig.APPLICATION_ID)

    override fun onCreate() {
        super.onCreate()
        middlewares()
        timber()
    }

    private fun middlewares() {
        Middlewares.configurations.add(
            MiddlewareConfiguration(
                condition = WrappingCondition.Always,
                factories = listOf { consumer -> LoggingMiddleware(consumer, { Timber.d(it) }) }
            )
        )

        Middlewares.configurations.add(
            MiddlewareConfiguration(
                condition = WrappingCondition.AllOf(
                    WrappingCondition.Conditional { BuildConfig.DEBUG },
                    WrappingCondition.AnyOf(
                        WrappingCondition.IsNamed,
                        WrappingCondition.IsStandalone
                    )
                ),
                factories = listOf { consumer ->
                    PlaybackMiddleware(
                        consumer,
                        recordStore
                    ) { Timber.d(it) }
                }
            )
        )

        Middlewares.configurations.add(
            MiddlewareConfiguration(
                condition = WrappingCondition.Always,
                factories = listOf { consumer -> IdeaPluginMiddleware(consumer, defaultStore) }
            )
        )
    }

    private fun timber() {
        val lumberYard = LumberYard.getInstance(this)
        lumberYard.cleanUp()
        Timber.plant(lumberYard.tree())
        Timber.plant(Timber.DebugTree())
    }
}

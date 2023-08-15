package com.badoo.mvicore.android

import com.badoo.mvicore.feature.FeatureScheduler

/**
 * Allows customisation of the MVICore Android integration.
 */
object MviCoreAndroidPlugins {
    @Volatile
    var mainThreadFeatureScheduler: FeatureScheduler = AndroidMainThreadFeatureScheduler.Default

    /**
     * Overrides the [AndroidMainThreadFeatureScheduler].
     */
    fun setMainThreadFeatureScheduler(schedulerProvider: () -> FeatureScheduler) {
        mainThreadFeatureScheduler = schedulerProvider()
    }

    /**
     * Resets the plugins back to the original state.
     */
    fun reset() {
        mainThreadFeatureScheduler = AndroidMainThreadFeatureScheduler.Default
    }
}

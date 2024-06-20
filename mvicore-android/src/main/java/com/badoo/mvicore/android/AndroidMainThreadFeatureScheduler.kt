package com.badoo.mvicore.android

import android.os.Looper
import com.badoo.mvicore.feature.FeatureScheduler
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * A feature scheduler that ensures that MVICore feature only manipulates state on the Android
 * main thread.
 *
 * It also uses the 'isOnFeatureThread' field to avoid observing on the main thread if it is already
 * the current thread.
 *
 * To help facilitate testing, you can override the scheduler using
 * [MviCoreAndroidPlugins.setMainThreadFeatureScheduler]
 */
object AndroidMainThreadFeatureScheduler : FeatureScheduler {
    private val featureSchedulerDelegate: FeatureScheduler
        get() = MviCoreAndroidPlugins.mainThreadFeatureScheduler

    override val scheduler: Scheduler
        get() = featureSchedulerDelegate.scheduler

    override val isOnFeatureThread: Boolean
        get() = featureSchedulerDelegate.isOnFeatureThread

    /**
     * The default implementation of the [AndroidMainThreadFeatureScheduler] which delegates to the
     * RxAndroid main thread scheduler
     */
    object Default : FeatureScheduler {
        override val scheduler: Scheduler
            get() = AndroidSchedulers.mainThread()

        override val isOnFeatureThread: Boolean
            get() = Looper.myLooper() == Looper.getMainLooper()
    }
}

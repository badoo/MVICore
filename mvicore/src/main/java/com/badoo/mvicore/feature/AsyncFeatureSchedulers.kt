package com.badoo.mvicore.feature

import io.reactivex.rxjava3.core.Scheduler

/**
 * A set of [Scheduler]s that change the threading behaviour of [BaseAsyncFeature]
 */
class AsyncFeatureSchedulers(
    /** Should be single-threaded. */
    val featureScheduler: Scheduler,
    val observationScheduler: Scheduler
)

package com.badoo.mvicore.feature

import io.reactivex.Scheduler

/**
 * A set of [Scheduler]s that change the threading behaviour of a [Feature]
 */
class FeatureSchedulers(
    /** Should be single-threaded. */
    val featureScheduler: Scheduler,
    val observationScheduler: Scheduler
)

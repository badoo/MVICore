package com.badoo.mvicore.feature

import io.reactivex.rxjava3.core.Scheduler

/**
 * A set of [Scheduler]s that change the threading behaviour of [BaseFeature]
 */
interface FeatureScheduler {
    /**
     * The scheduler that this feature executes on.
     * This must be single threaded, otherwise your feature will be non-deterministic.
     */
    val scheduler: Scheduler

    /**
     * Helps avoid sending a message to a thread if we are already on the thread.
     */
    val isOnFeatureThread: Boolean
}

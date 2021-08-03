package com.badoo.mvicore.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

class MVICoreSchedulers {
    companion object {
        internal var legacyScheduler: Scheduler? = null
            private set

        internal var observerScheduler: Scheduler? = null
            private set

        private val standardMainScheduler = Schedulers.from(Executors.newFixedThreadPool(1))
        var main = standardMainScheduler
            private set

        fun scheduler(scheduler: Scheduler?) =
            scheduler ?: legacyScheduler ?: main

        fun observerScheduler(scheduler: Scheduler?) =
            scheduler ?: observerScheduler ?: main

        fun setNewMainScheduler(scheduler: Scheduler) {
            main = scheduler
        }

        fun setNewLegacyScheduler(scheduler: Scheduler) {
            legacyScheduler = scheduler
        }

        fun setNewObserverScheduler(scheduler: Scheduler) {
            observerScheduler = scheduler
        }

        fun reset() {
            main = standardMainScheduler
        }
    }
}

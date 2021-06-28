package com.badoo.mvicore.android

import android.os.Process
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object MVICoreSchedulers {

    val FEATURE_SCHEDULER: Scheduler by lazy {
        Schedulers.from(
            Executors.newSingleThreadExecutor { runnable ->
                Thread {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                    runnable.run()
                }.apply {
                    name = "MVICore.FeatureThread"
                }
            }
        )
    }

    val OBSERVATION_SCHEDULER: Scheduler = AndroidSchedulers.mainThread()

}
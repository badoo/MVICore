package com.badoo.mvicore.feature

import io.reactivex.Scheduler
import io.reactivex.internal.schedulers.RxThreadFactory
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.ThreadFactory

object FeatureSchedulerFactory {
    /**
     * Creates a single threaded feature scheduler.
     */
    fun create(
        threadPrefix: String,
        threadPriority: Int = Thread.NORM_PRIORITY
    ): BaseFeature.FeatureScheduler {
        return object : BaseFeature.FeatureScheduler {
            private val singleThreadedThreadFactory by lazy {
                ThreadIdInterceptingThreadFactory(threadPrefix, threadPriority)
            }
            private val lazyScheduler by lazy {
                createScheduler(singleThreadedThreadFactory)
            }

            override val scheduler: Scheduler
                get() = lazyScheduler

            override val isOnFeatureThread: Boolean
                get() = Thread.currentThread().id == singleThreadedThreadFactory.getThreadId()
        }
    }

    private fun createScheduler(threadFactory: ThreadFactory) =
        RxJavaPlugins
            .createSingleScheduler(threadFactory)
            .apply { start() }

    /**
     * A thread factory which stores the thread id of the thread created.
     * This factory should only create one thread, otherwise it will throw an exception.
     */
    private class ThreadIdInterceptingThreadFactory(prefix: String, priority: Int) : ThreadFactory {
        private val delegate by lazy { RxThreadFactory(prefix, priority, false) }
        private var threadId: Long? = null

        fun getThreadId(): Long =
            requireNotNull(threadId) {
                "Thread Id was not found. The scheduler may not have created the thread yet"
            }

        private fun setThreadId(threadId: Long) {
            check(this.threadId == null) { "Multiple threads have been created" }
            this.threadId = threadId
        }

        override fun newThread(r: Runnable): Thread =
            delegate.newThread(r).also { setThreadId(it.id) }
    }
}

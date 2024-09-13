package com.badoo.mvicore.feature

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.internal.schedulers.RxThreadFactory
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.ThreadFactory

object FeatureSchedulers {
    /**
     * Creates a single threaded feature scheduler.
     */
    @JvmStatic
    fun createFeatureScheduler(
        threadPrefix: String,
        threadPriority: Int = Thread.NORM_PRIORITY
    ): FeatureScheduler {
        return object : FeatureScheduler {
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
     * A feature scheduler that is useful for unit testing.
     */
    object TrampolineFeatureScheduler : FeatureScheduler {
        override val scheduler: Scheduler = Schedulers.trampoline()

        override val isOnFeatureThread: Boolean = false
    }

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

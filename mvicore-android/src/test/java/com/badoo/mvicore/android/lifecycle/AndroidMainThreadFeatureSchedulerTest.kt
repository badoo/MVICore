package com.badoo.mvicore.android.lifecycle

import com.badoo.mvicore.android.AndroidMainThreadFeatureScheduler
import com.badoo.mvicore.android.MviCoreAndroidPlugins
import com.badoo.mvicore.feature.FeatureSchedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class AndroidMainThreadFeatureSchedulerTest {
    @AfterEach
    fun after() {
        MviCoreAndroidPlugins.reset()
    }

    @Test
    fun `GIVEN android main scheduler not overridden WHEN scheduler accessed THEN scheduler is android main thread scheduler`() {
        assertSame(AndroidSchedulers.mainThread(), AndroidMainThreadFeatureScheduler.scheduler)
    }

    @Test
    fun `GIVEN android main scheduler overridden with trampoline feature scheduler WHEN scheduler accessed THEN scheduler is trampoline scheduler`() {
        MviCoreAndroidPlugins.setMainThreadFeatureScheduler { FeatureSchedulers.TrampolineFeatureScheduler }

        assertSame(Schedulers.trampoline(), AndroidMainThreadFeatureScheduler.scheduler)
    }

    @Test
    fun `GIVEN android main scheduler overridden with trampoline feature scheduler AND reset WHEN scheduler accessed THEN scheduler is android main thread scheduler`() {
        MviCoreAndroidPlugins.setMainThreadFeatureScheduler { FeatureSchedulers.TrampolineFeatureScheduler }
        MviCoreAndroidPlugins.reset()

        assertSame(AndroidSchedulers.mainThread(), AndroidMainThreadFeatureScheduler.scheduler)
    }
}

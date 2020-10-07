package com.badoo.mvicoredemo.di.appscope.module

import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore
import com.badoo.mvicore.consumer.playback.MemoryRecordStore
import com.badoo.mvicore.debugdrawer.MviCoreControlsModule
import com.badoo.mvicoredemo.di.appscope.scope.AppScope
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber

@Module
class MviCoreModule {

    @Provides
    @AppScope
    fun recordStore(): RecordStore =
        MemoryRecordStore(
            playbackScheduler = AndroidSchedulers.mainThread(),
            logger = { Timber.d(it) }
        )

    @Provides
    fun debugDrawerControls(recordStore: RecordStore): MviCoreControlsModule =
        MviCoreControlsModule(
            recordStore
        )
}

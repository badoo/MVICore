package com.badoo.mvicoredemo.di.appscope.module

import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore
import com.badoo.mvicore.consumer.playback.MemoryRecordStore
import com.badoo.mvicore.debugdrawer.MviCoreControlsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MviCoreModule {

    @Provides
    @Singleton
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

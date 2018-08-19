package com.badoo.mvicoredemo.ui.common

import android.support.v7.app.AppCompatActivity
import com.badoo.mvicore.debugdrawer.MviCoreControlsModule
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.commons.BuildModule
import io.palaima.debugdrawer.commons.DeviceModule
import io.palaima.debugdrawer.commons.SettingsModule
import io.palaima.debugdrawer.network.quality.NetworkQualityModule
import io.palaima.debugdrawer.scalpel.ScalpelModule
import io.palaima.debugdrawer.timber.TimberModule
import javax.inject.Inject

abstract class DebugActivity : AppCompatActivity() {

    @Inject
    lateinit var playbackControlsAction: MviCoreControlsModule

    protected fun setupDebugDrawer() {
        val drawer = DebugDrawer.Builder(this)
            .modules(
                playbackControlsAction,
                NetworkQualityModule(this),
                ScalpelModule(this),
                TimberModule(),
                SettingsModule(),
                BuildModule(),
                DeviceModule()
            ).build()

        playbackControlsAction.drawer = drawer
    }
}

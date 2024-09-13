package com.badoo.mvicore.debugdrawer

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.FINISHED_PLAYBACK
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.IDLE
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.PLAYBACK
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.RECORDING
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.RecordKey
import io.palaima.debugdrawer.DebugDrawer
import io.palaima.debugdrawer.base.DebugModuleAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MviCoreControlsModule(
    private val recordStore: PlaybackMiddleware.RecordStore
) : DebugModuleAdapter() {

    private lateinit var startRecording: ImageButton
    private lateinit var stopRecording: ImageButton
    private lateinit var playback: ImageButton
    private lateinit var records: Spinner
    private val disposable = CompositeDisposable()
    var drawer: DebugDrawer? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        val layout = inflater.inflate(R.layout.playback_controls, parent, false)

        startRecording = layout.findViewById(R.id.startRecording)
        stopRecording = layout.findViewById(R.id.stopRecording)
        playback = layout.findViewById(R.id.playback)
        records = layout.findViewById(R.id.records)

        startRecording.setOnClickListener {
            recordStore.startRecording()
            drawer?.closeDrawer()
        }

        stopRecording.setOnClickListener {
            recordStore.stopRecording()
        }

        playback.setOnClickListener {
            (records.selectedItem as? RecordKey)?.let {
                recordStore.stopRecording()
                recordStore.playback(it)
                drawer?.closeDrawer()
            }
        }

        return layout
    }

    override fun onStart() {
        super.onStart()
        disposable.add(recordStore.records().subscribe {
            records.adapter = RecordsAdapter(startRecording.context, it)
        })
        disposable.add(recordStore.state().subscribe {
            when (it) {
                IDLE -> {
                    startRecording.enable()
                    stopRecording.disable()
                    playback.enable()
                }
                RECORDING -> {
                    startRecording.disable()
                    stopRecording.enable()
                    playback.disable()
                }
                FINISHED_PLAYBACK -> {
                    Toast.makeText(startRecording.context, R.string.finished_playback, Toast.LENGTH_SHORT).show()
                }
                PLAYBACK -> {
                    startRecording.disable()
                    stopRecording.disable()
                    playback.disable()
                }
            }
        })
    }

    private fun ImageButton.enable() {
        isEnabled = true
        isClickable = true
        background?.colorFilter  = null
        drawable?.colorFilter  = null
    }

    private fun ImageButton.disable() {
        isEnabled = false
        isClickable = false
        background?.setColorFilter(resources.getColor(R.color.grey_200), PorterDuff.Mode.SRC_IN)
        drawable?.setColorFilter(resources.getColor(R.color.grey_300), PorterDuff.Mode.SRC_IN)
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    class RecordsAdapter(
        context: Context,
        records: List<RecordKey>
    ) : ArrayAdapter<RecordKey>(context, R.layout.list_item_simple_small, records)
}

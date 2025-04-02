package com.badoo.mvicore.consumer.playback

import com.badoo.binder.Connection
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.Event
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.FINISHED_PLAYBACK
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.IDLE
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.PLAYBACK
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.PlaybackState.RECORDING
import com.badoo.mvicore.consumer.middleware.PlaybackMiddleware.RecordStore.RecordKey
import com.badoo.mvicore.consumer.util.Logger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class MemoryRecordStore(
    private val logger: Logger? = null,
    private val playbackScheduler: Scheduler
) : RecordStore {
    private val state: BehaviorSubject<PlaybackState> = BehaviorSubject.createDefault(IDLE)
    private val records: BehaviorSubject<List<RecordKey>> =
        BehaviorSubject.createDefault(emptyList())
    private val cachedEvents: MutableMap<Key<*, *>, MutableList<Event>> = mutableMapOf()
    private val lastElementBuffer: MutableMap<Key<*, *>, Any> = mutableMapOf()
    private var isRecording = false
    private var recordBaseTimestampNanos = 0L


    override fun startRecording() {
        cachedEvents.forEach { it.value.clear() }
        lastElementBuffer.forEach { cachedEvents[it.key]?.add(Event(0, it.value)) }
        logger?.invoke("MemoryRecordStore: STARTED RECORDING")
        isRecording = true
        recordBaseTimestampNanos = System.nanoTime()
        state.onNext(RECORDING)
    }

    override fun stopRecording() {
        if (isRecording) {
            val endSignal = eventFrom(EndSignal)
            cachedEvents.forEach { it.value.add(endSignal) }
            logger?.invoke("MemoryRecordStore: STOPPED RECORDING")
            isRecording = false
            recordBaseTimestampNanos = 0L
            state.onNext(IDLE)
        }
    }

    override fun <Out : Any, In : Any> register(
        middleware: PlaybackMiddleware<Out, In>,
        endpoints: Connection<Out, In>
    ) {
        cachedEvents[Key(middleware, endpoints)] = mutableListOf()
        updateRecords()
    }

    override fun <Out : Any, In : Any> unregister(
        middleware: PlaybackMiddleware<Out, In>,
        endpoints: Connection<Out, In>
    ) {
        val key = Key(middleware, endpoints)
        cachedEvents.remove(key)
        lastElementBuffer.remove(key)
        updateRecords()
    }

    override fun <Out : Any, In : Any> record(
        middleware: PlaybackMiddleware<Out, In>,
        endpoints: Connection<Out, In>,
        element: In
    ) {
        val key = Key(middleware, endpoints)
        lastElementBuffer[key] = element

        if (isRecording) {
            logger?.invoke("MemoryRecordStore: RECORDED element: [$element] on $endpoints")
            cachedEvents[key]!!.add(eventFrom(element))
        } else {
            logger?.invoke("MemoryRecordStore: SKIPPED element: [$element] on $endpoints")
        }
    }

    private fun eventFrom(obj: Any) =
        if (recordBaseTimestampNanos == 0L)
            throw IllegalStateException(
                "Don't create events when base timestamp is 0, you'll wait forever for the delay on playback. " +
                        "Check if you are in recording state?"
            )
        else Event(
            delayNanos = System.nanoTime() - recordBaseTimestampNanos,
            obj = obj
        )

    override fun records(): Observable<List<RecordKey>> =
        records

    override fun state(): Observable<PlaybackState> =
        state

    private fun updateRecords() {
        records.onNext(
            cachedEvents.keys
                .filter { !it.connection.isAnonymous() }
                .map { it.toRecordKey() }
                .sortedBy { it.name }
        )
    }

    override fun playback(recordKey: RecordKey) {
        if (isRecording) {
            throw IllegalStateException("Trying to playback while still recording")
        }

        cachedEvents
            .entries
            .first { it.key.id == recordKey.id }
            .let { (key, events) ->
                Observable.fromIterable(events)
                    .delay { Observable.timer(it.delayNanos, TimeUnit.NANOSECONDS) }
                    .observeOn(playbackScheduler)
                    .doOnNext { logger?.invoke("MemoryRecordStore: PLAYBACK: ts: ${it.delayNanos}, event: ${it.obj}") }
                    .map { it.obj }
                    .doOnSubscribe {
                        state.onNext(PLAYBACK)
                        key.middleWare.startPlayback()
                    }
                    .doOnTerminate {
                        logger?.invoke("MemoryRecordStore: PLAYBACK FINISHED")
                        state.onNext(FINISHED_PLAYBACK)
                        state.onNext(IDLE)
                        key.middleWare.stopPlayback()
                    }
                    .subscribe {
                        key.middleWare.replay(
                            // restore last state before playback started if needed
                            if (it == EndSignal) lastElementBuffer[key]
                            else it
                        )
                    }
            }
    }

    private data class Key<Out : Any, In : Any>(
        val middleWare: PlaybackMiddleware<Out, In>,
        val connection: Connection<Out, In>
    ) {
        val id = hashCode()

        fun toRecordKey() =
            RecordKey(
                id = id,
                name = connection.name ?: "anonymous"
            )
    }

    internal object EndSignal
}

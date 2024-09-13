package com.badoo.mvicore.consumer.middleware

import com.badoo.binder.Connection
import com.badoo.binder.middleware.base.Middleware
import com.badoo.mvicore.consumer.util.Logger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer

open class PlaybackMiddleware<Out: Any, In: Any>(
    wrapped: Consumer<In>,
    private val recordStore: RecordStore,
    private val logger: Logger? = null
) : Middleware<Out, In>(wrapped) {

    private var isInPlaybackMode: Boolean = false

    override fun onBind(connection: Connection<Out, In>) {
        super.onBind(connection)
        logger?.invoke("PlaybackMiddleware: Creating record store entry for $connection")
        recordStore.register(this, connection)
    }

    override fun onElement(connection: Connection<Out, In>, element: In) {
        super.onElement(connection, element)
        logger?.invoke("PlaybackMiddleware: Sending to record store: [$element] on $connection")
        recordStore.record(this, connection, element)
    }

    override fun onComplete(connection: Connection<Out, In>) {
        super.onComplete(connection)
        logger?.invoke("PlaybackMiddleware: Removing record store entry for binding $connection")
        recordStore.unregister(this, connection)
    }

    fun startPlayback() {
        isInPlaybackMode = true
    }

    fun isInPlayback(): Boolean =
        isInPlaybackMode

    fun stopPlayback() {
        isInPlaybackMode = false
    }

    fun replay(obj: Any?) {
        if (isInPlaybackMode) {
            logger?.invoke("PlaybackMiddleware: PLAYBACK: $obj")
            obj?.let {
                super.accept(obj as In)
            }
        }
    }

    override fun accept(t: In) {
        if (!isInPlaybackMode) {
            super.accept(t)
        }
    }

    interface RecordStore {
        fun startRecording()
        fun stopRecording()
        fun <Out: Any, In: Any> register(middleware: PlaybackMiddleware<Out, In>, endpoints: Connection<Out, In>)
        fun <Out: Any, In: Any> unregister(middleware: PlaybackMiddleware<Out, In>, endpoints: Connection<Out, In>)
        fun <Out: Any, In: Any> record(middleware: PlaybackMiddleware<Out, In>, endpoints: Connection<Out, In>, element: In)
        fun playback(recordKey: RecordKey)
        fun records(): Observable<List<RecordKey>>
        fun state(): Observable<PlaybackState>

        enum class PlaybackState {
            IDLE, RECORDING, PLAYBACK, FINISHED_PLAYBACK
        }

        data class RecordKey(
            val id: Int,
            val name: String
        ) {
            override fun toString(): String = name

        }

        data class Event(
            val delayNanos: Long,
            val obj: Any
        )
    }
}


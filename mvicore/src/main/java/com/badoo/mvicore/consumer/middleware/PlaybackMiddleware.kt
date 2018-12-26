package com.badoo.mvicore.consumer.middleware

import com.badoo.mvicore.binder.Connection
import com.badoo.mvicore.consumer.middleware.base.Middleware
import com.badoo.mvicore.consumer.util.Logger
import io.reactivex.Observable
import io.reactivex.functions.Consumer

open class PlaybackMiddleware<T : Any>(
    wrapped: Consumer<T>,
    private val recordStore: RecordStore,
    private val logger: Logger? = null
) : Middleware<Any, T>(wrapped) {

    private var isInPlaybackMode: Boolean = false

    override fun onBind(connection: Connection<Any, T>) {
        super.onBind(connection)
        logger?.invoke("PlaybackMiddleware: Creating record store entry for $connection")
        recordStore.register(this, connection)
    }

    override fun onElement(connection: Connection<Any, T>, element: T) {
        super.onElement(connection, element)
        logger?.invoke("PlaybackMiddleware: Sending to record store: [$element] on $connection")
        recordStore.record(this, connection, element)
    }

    override fun onComplete(connection: Connection<Any, T>) {
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
                super.accept(obj as T)
            }
        }
    }

    override fun accept(t: T) {
        if (!isInPlaybackMode) {
            super.accept(t)
        }
    }

    interface RecordStore {
        fun startRecording()
        fun stopRecording()
        fun <T : Any> register(middleware: PlaybackMiddleware<T>, endpoints: Connection<Any, T>)
        fun <T : Any> unregister(middleware: PlaybackMiddleware<T>, endpoints: Connection<Any, T>)
        fun <T : Any> record(middleware: PlaybackMiddleware<T>, endpoints: Connection<Any, T>, element: T)
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


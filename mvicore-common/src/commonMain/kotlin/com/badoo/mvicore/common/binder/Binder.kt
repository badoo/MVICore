package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.CompositeCancellable
import com.badoo.mvicore.common.PublishSource
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.connect
import com.badoo.mvicore.common.lifecycle.Lifecycle
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.END
import com.badoo.mvicore.common.source
import com.badoo.mvicore.common.sources.DelayUntilSource
import com.badoo.reaktive.utils.atomic.AtomicBoolean
import com.badoo.reaktive.utils.atomic.AtomicReference
import com.badoo.reaktive.utils.atomic.update

/**
 * Establishes connections between [Source] and [Sink] endpoints
 * NOTE: binder is not thread safe. All the emissions should happen on single thread (preferably main).
 */
interface Binder : Cancellable {
    fun <Out, In> connect(connection: Connection<Out, In>)
}

internal class SimpleBinder(init: Binder.() -> Unit) : Binder {
    private val cancellables = CompositeCancellable()

    /**
     * Stores internal end of every `emitter` connected through binder
     * Allows for propagation of events in case emission cancels the binder
     * E.g.:
     * from -> internalSource -> to // Events from `from` are propagated to `to`
     * #cancel()
     * from xx internalSource -> to // Remaining events from `internalSource` are propagated to `to`
     */
    private val internalSourceCache = AtomicReference(emptyMap<Source<*>, PublishSource<*>>())

    /**
     * Delay events emitted on subscribe until `init` lambda is executed
     */
    private val initialized = source(initialValue = false)

    init {
        init()
        initialized.accept(true)
    }

    override fun <Out, In> connect(connection: Connection<Out, In>) {
        val internalSource = getInternalSourceFor(connection.from!!)
        val transformedSource = if (connection.connector != null) {
            connection.connector.invoke(internalSource)
        } else {
            internalSource as Source<In>
        }

        val delayInitialize = if (initialized.value!!) {
            transformedSource
        } else {
            DelayUntilSource(initialized, transformedSource)
        }

        delayInitialize.connect(connection.to as Sink<In>)
    }

    private fun <T> getInternalSourceFor(from: Source<T>): PublishSource<T> {
        val cachedSource = internalSourceCache.value[from]
        return if (cachedSource != null) {
            cachedSource as PublishSource<T>
        } else {
            source<T>().also { source ->
                cancellables += from.connect(source)
                internalSourceCache.update { it + (from to source) }
            }
        }
    }

    override fun cancel() {
        cancellables.cancel()
        internalSourceCache.update { emptyMap() }
    }

    override val isCancelled: Boolean
        get() = cancellables.isCancelled
}

internal class LifecycleBinder(lifecycle: Lifecycle, init: Binder.() -> Unit) : Binder {
    private var lifecycleActive = AtomicBoolean(false)
    private val cancellables = CompositeCancellable()
    private val innerBinder = SimpleBinder(init)
    private val connections = AtomicReference(emptyArray<Connection<*, *>>())

    init {
        cancellables += lifecycle.connect {
            when (it) {
                BEGIN -> connect()
                END -> disconnect()
            }
        }
        cancellables += innerBinder
    }

    override fun <Out, In> connect(connection: Connection<Out, In>) {
        connections.update { it + connection }
        if (lifecycleActive.value) {
            innerBinder.connect(connection)
        }
    }

    private fun connect() {
        if (lifecycleActive.value) return

        lifecycleActive.value = true
        connections.value.forEach { innerBinder.connect(it) }
    }

    private fun disconnect() {
        if (!lifecycleActive.value) return

        lifecycleActive.value = false
        innerBinder.cancel()
    }

    override fun cancel() {
        cancellables.cancel()
        connections.update { emptyArray() }
    }

    override val isCancelled: Boolean
        get() = cancellables.isCancelled

}

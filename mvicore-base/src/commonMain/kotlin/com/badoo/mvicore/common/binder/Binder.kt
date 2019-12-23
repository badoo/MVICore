package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.SimpleSource
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.END
import com.badoo.mvicore.common.source
import com.badoo.mvicore.common.sources.DelayUntilSource

/**
 * Establishes connections between [Source] and [Sink] endpoints
 * NOTE: binder is not thread safe. All the emissions should happen on single thread (preferably main).
 */
abstract class Binder : Cancellable {
    internal abstract fun <Out, In> connect(connection: Connection<Out, In>)
}

internal class SimpleBinder(init: Binder.() -> Unit) : Binder() {
    private val cancellables = mutableListOf<Cancellable>()

    /**
     * Stores internal end of every `emitter` connected through binder
     * Allows for propagation of events in case emission disposes the binder
     * E.g.:
     * from -> internalSource -> to // Events from `from` are propagated to `to`
     * #dispose()
     * from xx internalSource -> to // Remaining events from `internalSource` are propagated to `to`
     */
    private val fromToInternalSource = mutableMapOf<Source<*>, SimpleSource<*>>()

    /**
     * Delay events emitted on subscribe until `init` lambda is executed
     */
    private val initialized = source(initialValue = false)

    init {
        init()
        initialized(true)
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

        delayInitialize.connect(connection.to)
    }

    private fun <T> getInternalSourceFor(from: Source<T>): SimpleSource<T> =
        fromToInternalSource.getOrPut(from) {
            source<T>().also { cancellables += from.connect(it) }
        } as SimpleSource<T>

    override fun cancel() {
        cancellables.forEach { it.cancel() }
        fromToInternalSource.clear()
        cancellables.clear()
    }
}

internal class LifecycleBinder(lifecycle: Lifecycle, init: Binder.() -> Unit) : Binder() {
    private var lifecycleActive = false
    private val cancellables = mutableListOf<Cancellable>()
    private val innerBinder = SimpleBinder(init)
    private val connections = mutableListOf<Connection<*, *>>()

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
        connections += connection
        if (lifecycleActive) {
            innerBinder.connect(connection)
        }
    }

    private fun connect() {
        if (lifecycleActive) return

        lifecycleActive = true
        connections.forEach { innerBinder.connect(it) }
    }

    private fun disconnect() {
        if (!lifecycleActive) return

        lifecycleActive = false
        innerBinder.cancel()
    }

    override fun cancel() {
        cancellables.forEach { it.cancel() }
        connections.clear()
    }

}

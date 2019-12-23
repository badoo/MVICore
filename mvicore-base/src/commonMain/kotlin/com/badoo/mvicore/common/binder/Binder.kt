package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.SimpleSource
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.END
import com.badoo.mvicore.common.source

abstract class Binder : Cancellable {
    internal abstract fun <Out, In> connect(connection: Connection<Out, In>)
}

fun binder(): Binder = SimpleBinder()
fun binder(lifecycle: Lifecycle): Binder = LifecycleBinder(lifecycle)

fun <In> Binder.bind(connection: Pair<Source<In>, Sink<In>>) {
    val (from, to) = connection
    connect(Connection(from = from, to = to))
}

fun <Out, In> Binder.bind(connection: Connection<Out, In>) {
    connect(connection)
}

internal class SimpleBinder : Binder() {
    private val cancellables = mutableListOf<Cancellable>()
    private val fromToInternalSource = mutableMapOf<Source<*>, SimpleSource<*>>()

    override fun <Out, In> connect(connection: Connection<Out, In>) {
        val internalSource = getInternalSourceFor(connection.from!!)
        val outSource = if (connection.connector != null) {
            connection.connector.invoke(internalSource)
        } else {
            internalSource as Source<In>
        }
        outSource.connect(connection.to)
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

internal class LifecycleBinder(lifecycle: Lifecycle) : Binder() {
    private var lifecycleActive = false
    private val cancellables = mutableListOf<Cancellable>()
    private val innerBinder = SimpleBinder()
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

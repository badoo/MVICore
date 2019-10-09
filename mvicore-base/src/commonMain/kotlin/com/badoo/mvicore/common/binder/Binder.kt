package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.BEGIN
import com.badoo.mvicore.common.lifecycle.Lifecycle.Event.END

interface Binder : Cancellable {
    fun <T> bind(connection: Pair<Source<T>, Sink<T>>)
    fun <Out, In> bind(connection: Connection<Out, In>)
}

fun Binder(): Binder = SimpleBinder()
fun Binder(lifecycle: Lifecycle): Binder = LifecycleBinder(lifecycle)

internal class SimpleBinder : Binder {
    private val cancellables = mutableListOf<Cancellable>()

    override fun <T> bind(connection: Pair<Source<T>, Sink<T>>) {
        val (from, to) = connection
        bind(Connection(from = from, to = to))
    }

    override fun <Out, In> bind(connection: Connection<Out, In>) {
        val out = connection.connector?.let { it(connection.from!!) } ?: (connection.from as Source<In>)
        cancellables += out.connect(connection.to)
    }

    override fun cancel() {
        cancellables.forEach { it.cancel() }
        cancellables.clear()
    }
}

internal class LifecycleBinder(private val lifecycle: Lifecycle) : Binder {
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

    override fun <T> bind(connection: Pair<Source<T>, Sink<T>>) {
        val (from, to) = connection
        bind(Connection(from = from, to = to))
    }

    override fun <Out, In> bind(connection: Connection<Out, In>) {
        connections += connection
        if (lifecycleActive) {
            innerBinder.bind(connection)
        }
    }

    private fun connect() {
        if (lifecycleActive) return

        lifecycleActive = true
        connections.forEach { innerBinder.bind(it) }
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

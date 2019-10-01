package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle

class Binder(private val lifecycle: Lifecycle? = null): Cancellable {
    private val cancellables = mutableListOf<Cancellable>()

    fun <T> bind(connection: Pair<Source<T>, Sink<T>>) {
        val (from, to) = connection
        bind(Connection(from = from, to = to))
    }

    fun <Out, In> bind(connection: Connection<Out, In>) {
        val out = connection.connector?.let { it(connection.from!!) } ?: (connection.from as Source<In>)
        cancellables += out.connect(connection.to)
    }

    override fun cancel() {
        cancellables.forEach { it.cancel() }
        cancellables.clear()
    }
}

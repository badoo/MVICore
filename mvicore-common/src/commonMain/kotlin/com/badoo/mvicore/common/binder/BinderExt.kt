package com.badoo.mvicore.common.binder

import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source
import com.badoo.mvicore.common.lifecycle.Lifecycle

fun binder(init: Binder.() -> Unit = { }): Binder = SimpleBinder(init)
fun binder(lifecycle: Lifecycle, init: Binder.() -> Unit = { }): Binder = LifecycleBinder(lifecycle, init)

fun <In> Binder.bind(connection: Pair<Source<In>, Sink<In>>) {
    val (from, to) = connection
    connect(Connection(from = from, to = to))
}

fun <Out, In> Binder.bind(connection: Connection<Out, In>) {
    connect(connection)
}

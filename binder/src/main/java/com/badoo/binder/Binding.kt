package com.badoo.binder

import com.badoo.binder.middleware.base.Middleware
import io.reactivex.ObservableSource
import io.reactivex.subjects.UnicastSubject

internal class Binding(
    val connection: Connection<*, *>,
    val middleware: Middleware<*, *>?
) {

    var source: ObservableSource<*>? = null
        private set

    fun accumulate() {
        source = connection.from?.let { source ->
            UnicastSubject.create<Any>()
                .also { observer -> source.subscribe(observer) }
        }
    }

    fun drain() {
        (connection.from as? Drainable)?.drain()
    }
}

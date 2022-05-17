package com.badoo.binder

import com.badoo.binder.middleware.base.Middleware
import io.reactivex.subjects.UnicastSubject

internal class Binding(
    val connection: Connection<*, *>,
    val middleware: Middleware<*, *>?
) {

    var source: UnicastSubject<*>? = null
        private set

    fun accumulate() {
        source = connection.from?.let { source ->
            UnicastSubject.create<Any>()
                .also { observer -> source.subscribe(observer) }
        }
    }
}
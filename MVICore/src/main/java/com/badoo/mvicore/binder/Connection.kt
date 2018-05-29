package com.badoo.mvicore.binder

import com.badoo.mvicore.extension.mapNotNull
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

data class Connection<T>(
    val from: ObservableSource<out T>? = null,
    val to: Consumer<in T>,
    val name: String? = null
) {
    companion object {
        private const val ANONYMOUS: String = "anonymous"
    }

    fun isAnonymous(): Boolean =
        name == null

    override fun toString(): String =
        "<${name ?: ANONYMOUS}> (${from ?: "?"} --> $to)"
}

infix fun <Out, In> Pair<ObservableSource<out Out>, Consumer<in In>>.using(transformer: (Out) -> In?) =
    Connection(
        from = Observable
            .wrap(first)
            .mapNotNull(transformer),
        to = second
    )

infix fun <T> Pair<ObservableSource<out T>, Consumer<in T>>.named(name: String) =
    Connection(
        from = first,
        to = second,
        name = name
    )

infix fun <T> Connection<T>.named(name: String) =
    copy(
        name = name
    )

package com.badoo.mvicore.binder

import com.badoo.mvicore.extension.mapNotNull
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

data class Connection<Out, In>(
    val from: ObservableSource<Out>? = null,
    val to: Consumer<In>,
    val transformer: ((Out) -> In?)? = null,
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

infix fun <Out, In> Pair<ObservableSource<Out>, Consumer<In>>.using(transformer: (Out) -> In?): Connection<Out, In> =
    Connection(
        from = first,
        to = second,
        transformer = transformer
    )

infix fun <T> Pair<ObservableSource<T>, Consumer<T>>.named(name: String): Connection<T, T> =
    Connection(
        from = first,
        to = second,
        name = name
    )

infix fun <Out, In> Connection<Out, In>.named(name: String) =
    copy(
        name = name
    )

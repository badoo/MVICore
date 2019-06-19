package com.badoo.mvicore.binder

import com.badoo.mvicore.connector.Connector
import com.badoo.mvicore.connector.NotNullConnector
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

data class Connection<Out, In>(
    val from: ObservableSource<out Out>? = null,
    val to: Consumer<in In>,
    val connector: Connector<Out, In>? = null,
    val name: String? = null
) {
    companion object {
        private const val ANONYMOUS: String = "anonymous"
    }

    fun isAnonymous(): Boolean =
        name == null

    override fun toString(): String =
        "<${name ?: ANONYMOUS}> (${from ?: "?"} --> $to${connector?.let { " using $it" } ?: ""})"
}

infix fun <Out, In> Pair<ObservableSource<out Out>, Consumer<in In>>.using(transformer: (Out) -> In?): Connection<Out, In> =
    using(NotNullConnector(transformer))

infix fun <Out, In> Pair<ObservableSource<out Out>, Consumer<in In>>.using(connector: Connector<Out, In>): Connection<Out, In> =
    Connection(
        from = first,
        to = second,
        connector = connector
    )

infix fun <T> Pair<ObservableSource<out T>, Consumer<in T>>.named(name: String): Connection<T, T> =
    Connection(
        from = first,
        to = second,
        name = name
    )

infix fun <Out, In> Connection<Out, In>.named(name: String) =
    copy(
        name = name
    )

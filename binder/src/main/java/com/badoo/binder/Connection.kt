package com.badoo.binder

import com.badoo.binder.connector.Connector
import com.badoo.binder.connector.NotNullConnector
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer

data class Connection<Out : Any, In : Any>(
    val from: ObservableSource<out Out>? = null,
    val to: Consumer<in In>,
    val connector: Connector<Out, In>? = null,
    val name: String? = null,
    val observeScheduler: Scheduler? = null,
) {
    companion object {
        private const val ANONYMOUS: String = "anonymous"
    }

    fun isAnonymous(): Boolean =
        name == null

    override fun toString(): String =
        "<${name ?: ANONYMOUS}> (${from ?: "?"} --> $to${connector?.let { " using $it" } ?: ""})"
}

infix fun <Out : Any, In : Any> Pair<ObservableSource<out Out>, Consumer<in In>>.using(transformer: (Out) -> In?): Connection<Out, In> =
    using(NotNullConnector(transformer))

infix fun <Out : Any, In : Any> Pair<ObservableSource<out Out>, Consumer<in In>>.using(connector: Connector<Out, In>): Connection<Out, In> =
    Connection(
        from = first,
        to = second,
        connector = connector
    )

infix fun <T : Any> Pair<ObservableSource<out T>, Consumer<in T>>.named(name: String): Connection<T, T> =
    Connection(
        from = first,
        to = second,
        name = name
    )

infix fun <T : Any> Pair<ObservableSource<out T>, Consumer<in T>>.observeOn(scheduler: Scheduler): Connection<T, T> =
    Connection(
        from = first,
        to = second,
        observeScheduler = scheduler
    )

infix fun <Out : Any, In : Any> Connection<Out, In>.named(name: String) =
    copy(
        name = name
    )

infix fun <Out : Any, In : Any> Connection<Out, In>.observeOn(scheduler: Scheduler) =
    copy(
        observeScheduler = scheduler
    )

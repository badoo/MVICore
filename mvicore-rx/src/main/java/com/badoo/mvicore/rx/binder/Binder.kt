package com.badoo.mvicore.rx.binder

import com.badoo.mvicore.common.binder.Binder
import com.badoo.mvicore.common.binder.Connection
import com.badoo.mvicore.common.binder.bind
import com.badoo.mvicore.common.binder.using
import com.badoo.mvicore.rx.toSink
import com.badoo.mvicore.rx.toSource
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

fun <T> Binder.bind(connection: Pair<ObservableSource<T>, Consumer<T>>) {
    bind(connection.first.toSource() to connection.second.toSink())
}

infix fun <Out, In> Pair<ObservableSource<Out>, Consumer<In>>.using(mapper: (Out) -> In?): Connection<Out, In> =
    first.toSource() to second.toSink() using mapper

infix fun <Out, In> Pair<ObservableSource<Out>, Consumer<In>>.using(connector: Connector<Out, In>): Connection<Out, In> =
    first.toSource() to second.toSink() using connector.toCommonConnector()

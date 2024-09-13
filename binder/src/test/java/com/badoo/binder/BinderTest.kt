package com.badoo.binder

import com.badoo.binder.connector.Connector
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.jupiter.api.Test

class BinderTest {

    private val binder = Binder()
    private val source = PublishSubject.create<Int>()
    private val consumer = TestConsumer<String>()

    @Test
    fun `binder connects with plain transformer`() {
        binder.bind(source to consumer using IntToString)

        source.onNext(0)
        consumer.assertValues("0")
    }

    @Test
    fun `binder connects with connector`() {
        binder.bind(source to consumer using TestConnector)

        source.onNext(0)
        consumer.assertValues("0", "1")
    }

    @Test
    fun `covariant endpoints compile for pair`() {
        val anyConsumer = TestConsumer<Any>()
        binder.bind(source to anyConsumer)
    }

    @Test
    fun `covariant endpoints compile for connection dsl`() {
        val anyConsumer = TestConsumer<Any>()
        binder.bind(source to anyConsumer using IntToString)
    }

    object IntToString: (Int) -> String {
        override fun invoke(it: Int): String = it.toString()
    }

    object TestConnector: Connector<Int, String> {
        override fun invoke(it: ObservableSource<out Int>): ObservableSource<String> =
            Observable.wrap(it).flatMap {
                Observable.just(it.toString(), (it + 1).toString())
            }
    }
}

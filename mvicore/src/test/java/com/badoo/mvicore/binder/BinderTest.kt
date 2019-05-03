package com.badoo.mvicore.binder

import com.badoo.mvicore.TestConsumer
import com.badoo.mvicore.assertValues
import com.badoo.mvicore.connector.Connector
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.subjects.PublishSubject
import org.junit.Test

class BinderTest {

    private val binder = Binder()
    private val source = PublishSubject.create<Int>()
    private val consumer = TestConsumer<String>()

    @Test
    fun `binder connects with plain transformer`() {
        binder.bind(source to consumer using { it.toString() })

        source.onNext(0)
        consumer.assertValues("0")
    }

    @Test
    fun `binder connects with observable transformer`() {
        binder.bind(source to consumer using TestConnector)

        source.onNext(0)
        consumer.assertValues("0", "1")
    }

    object TestConnector: Connector<Int, String> {
        override fun invoke(it: Int): Observable<String> =
            just(it.toString(), (it + 1).toString())
    }
}
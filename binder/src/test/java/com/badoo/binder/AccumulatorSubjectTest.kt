package com.badoo.binder

import com.badoo.binder.lifecycle.Lifecycle
import com.badoo.binder.lifecycle.ManualLifecycle
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Test

class AccumulatorSubjectTest {

    @Test
    fun `GIVEN the producer is an accumulator AND the consumer subscribes after the lifecycle started AND before the drain THEN consumes all the events produced`() {
        var score = 0
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val producer = AccumulatorSubject.create<Int>()
        val consumer = PublishSubject.create<Int>()
        val testObserver = consumer.test()
        val binder = Binder(lifecycle)

        producer.accept(score++)
        producer.accept(score++)
        lifecycle.begin()
        binder.bind(producer to Consumer { consumer.onNext(it) })
        binder.drain()

        testObserver.onComplete()
        testObserver.assertValues(0, 1)
    }

    @Test
    fun `GIVEN the producer is an accumulator AND the consumer subscribes after lifecycle started AND before the drain AND producer produces an event after the drain THEN consumes all the produced events`() {
        var score = 0
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val producer = AccumulatorSubject.create<Int>()
        val consumer = PublishSubject.create<Int>()
        val testObserver = consumer.test()
        val binder = Binder(lifecycle)

        producer.accept(score++)
        producer.accept(score++)
        lifecycle.begin()
        binder.bind(producer to Consumer { consumer.onNext(it) })
        binder.drain()
        producer.accept(score++)

        testObserver.onComplete()
        testObserver.assertValues(0, 1, 2)
    }

    @Test
    fun `GIVEN the producer is an accumulator AND the consumer subscribes after the lifecycle started AND after the drain THEN the consumer consumes only the events produced after the drain`() {
        var score = 0
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val producer = AccumulatorSubject.create<Int>()
        val consumer = PublishSubject.create<Int>()
        val testObserver = consumer.test()
        val binder = Binder(lifecycle)

        producer.accept(score++)
        producer.accept(score++)
        lifecycle.begin()
        binder.drain()
        binder.bind(producer to Consumer { consumer.onNext(it) })
        producer.accept(score++)

        testObserver.onComplete()
        testObserver.assertValues(2)
    }

    @Test
    fun `GIVEN the producer is an accumulator WHEN the consumer subscribes after the lifecycle started AND before the drain THEN should receive events produced before the drain and published when the lifecycle is active`() {
        var score = 0
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val producer = AccumulatorSubject.create<Int>()
        val consumer = PublishSubject.create<Int>()
        val testObserver = consumer.test()
        val binder = Binder(lifecycle)

        producer.accept(score++)
        producer.accept(score++)
        lifecycle.begin()
        binder.bind(producer to Consumer { consumer.onNext(it) })
        binder.drain()
        lifecycle.end()
        producer.accept(score++)
        lifecycle.begin()
        producer.accept(score++)

        testObserver.onComplete()
        testObserver.assertValues(0, 1, 3)
    }

    @Test
    fun `GIVEN the producer is an accumulator WHEN the consumer subscribes after the lifecycle restarted THEN should receive only the events after the restart when the lifecycle is active`() {
        var score = 0
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val producer = AccumulatorSubject.create<Int>()
        val consumer = PublishSubject.create<Int>()
        val testObserver = consumer.test()
        val binder = Binder(lifecycle)

        producer.accept(score++)
        producer.accept(score++)
        lifecycle.begin()
        binder.drain()
        lifecycle.end()
        binder.bind(producer to Consumer { consumer.onNext(it) })
        producer.accept(score++)
        lifecycle.begin()
        producer.accept(score++)

        testObserver.onComplete()
        testObserver.assertValues(3)
    }

}

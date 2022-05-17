package com.badoo.binder

import com.badoo.binder.lifecycle.Lifecycle
import com.badoo.binder.lifecycle.ManualLifecycle
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlin.test.assertEquals
import org.junit.Test

class BinderMissingPreBindEventsTest {

    @Test
    fun `consumer consumes the events produced before the binding`() {
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val scoreConsumer = ScoreConsumer()
        val testObserver = scoreConsumer.state.test()
        val scoreState = ScoreState()
        val binder = Binder(lifecycle)
        binder.bind(scoreState to scoreConsumer using { state -> ScoreConsumer.State(state) })
        binder.bind(scoreConsumer to scoreState using { event ->
            when (event) {
                ScoreConsumer.Event.Start -> ScoreState.Message.Start
            }
        })

        lifecycle.begin()

        testObserver.onComplete()
        testObserver.assertValueCount(2)
        assertEquals(PENDING, testObserver.values()[0].points)
        assertEquals(INITIAL, testObserver.values()[1].points)
    }

    private class ScoreState(
        private val events: BehaviorSubject<Int> = BehaviorSubject.create<Int>()
    ) : ObservableSource<Int> by events, Consumer<ScoreState.Message> {

        var score = PENDING
            private set

        init {
            accept(Message.Prepare)
        }

        sealed class Message {
            object Prepare : Message()
            object Start : Message()
        }

        override fun accept(message: Message) = when (message) {
            Message.Prepare -> events.onNext(score)
            Message.Start -> {
                score = INITIAL
                events.onNext(score)
            }
        }
    }

    private class ScoreConsumer(
        private val events: PublishSubject<Event> = PublishSubject.create()
    ) : ObservableSource<ScoreConsumer.Event> by events, Consumer<ScoreConsumer.State> {

        val state = PublishSubject.create<State>()

        override fun accept(state: State) {
            this.state.onNext(state)
            if (state.points == PENDING) {
                events.onNext(Event.Start)
            }
        }

        sealed class Event {
            object Start : Event()
        }

        data class State(val points: Int)
    }

    private companion object {
        const val PENDING = -1
        const val INITIAL = 0
    }
}

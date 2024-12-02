package com.badoo.binder

import com.badoo.binder.lifecycle.Lifecycle
import com.badoo.binder.lifecycle.ManualLifecycle
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Test

class BinderMissingPreBindEventsTest {

    @Test
    fun `GIVEN the producer stores the latest state WHEN consumer reacts to the latest event produced before the binding THEN should receives the corresponding update`() {
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val consumerEvents = PublishSubject.create<ScoreConsumer.Event>()
        val scoreConsumer = ScoreConsumer(consumerEvents, onStateUpdate = {
            if (it.points == 0) {
                consumerEvents.onNext(ScoreConsumer.Event.Score)
            }
        })
        val testObserver = scoreConsumer.inbox.test()
        val scoreState = ScoreState()
        val binder = Binder(lifecycle)
        bind(binder, scoreState, scoreConsumer)

        lifecycle.begin()

        testObserver.onComplete()
        testObserver.assertValues(
            ScoreConsumer.State(0),
            ScoreConsumer.State(1),
        )
    }

    @Test
    fun `GIVEN the producer stores the latest state WHEN consumer reacts to the latest event produced before the binding THEN an additional consumer should consume the corresponding event`() {
        val lifecycle: ManualLifecycle = Lifecycle.manual()
        val consumerEvents = AccumulatorSubject.create<ScoreConsumer.Event>()
        val scoreConsumer = ScoreConsumer(consumerEvents, onStateUpdate = {
            if (it.points == 0) {
                consumerEvents.accept(ScoreConsumer.Event.Score)
            }
        })
        val scoreState = ScoreState()
        val eventConsumerInbox = PublishSubject.create<ScoreConsumer.Event>()
        val eventConsumer = Consumer<ScoreConsumer.Event> { eventConsumerInbox.onNext(it) }
        val testObserver = eventConsumerInbox.test()
        val binder = Binder(lifecycle)
        bind(binder, scoreState, scoreConsumer)
        binder.bind(scoreConsumer to eventConsumer)

        lifecycle.begin()

        testObserver.onComplete()
        testObserver.assertValues(ScoreConsumer.Event.Score)
    }

    private class ScoreState(
        private val events: BehaviorSubject<State> = BehaviorSubject.createDefault(State.Idle)
    ) : ObservableSource<ScoreState.State> by events, Consumer<ScoreState.Message> {

        sealed interface State {
            object Idle : State
            data class InProgress(val score: Int) : State
        }

        private var state: State? = events.value

        init {
            accept(Message.Prepare)
        }

        sealed class Message {
            object Prepare : Message()
            object Score : Message()
        }

        override fun accept(message: Message) {
            state = when (message) {
                Message.Prepare -> State.InProgress(0)
                Message.Score -> when (state) {
                    is State.InProgress -> (state as State.InProgress).copy(score = (state as State.InProgress).score.inc())
                    else -> error("Not valid state")
                }
            }
            state?.also { events.onNext(it) }
        }
    }

    private class ScoreConsumer(
        private val events: ObservableSource<Event>,
        private val onStateUpdate: ((state: State) -> Unit)?
    ) : ObservableSource<ScoreConsumer.Event> by events,
        Consumer<ScoreConsumer.State> {

        data class State(val points: Int?)

        sealed interface Event {
            object Score : Event
        }

        val inbox = PublishSubject.create<State>()

        override fun accept(state: State) {
            inbox.onNext(state)
            onStateUpdate?.invoke(state)
        }
    }

    private fun bind(
        binder: Binder,
        scoreState: ScoreState,
        scoreConsumer: ScoreConsumer,
    ) {
        (scoreState to scoreConsumer using { state ->
            when (state) {
                is ScoreState.State.Idle -> ScoreConsumer.State(null)
                is ScoreState.State.InProgress -> ScoreConsumer.State(state.score)
            }
        })

        binder.bind(scoreConsumer to scoreState using { event ->
            when (event) {
                is ScoreConsumer.Event.Score -> ScoreState.Message.Score
            }
        })
    }
}

package com.badoo.mvicore2.store.internal

import com.badoo.mvicore2.store.Engine
import com.badoo.mvicore2.store.Engine.Actor
import io.reactivex.ObservableSource
import io.reactivex.subjects.BehaviorSubject

internal class DefaultEngine<Wish : Any, State : Any, Effect: Any>(
        initialState: State,
        private val actor: Actor<Wish, State, Effect>,
        private val reducer: Engine.Reducer<State, Effect>,
        private val states: BehaviorSubject<State> = BehaviorSubject.createDefault(initialState)
) : Engine<Wish, State>,
        ObservableSource<State> by states {

    override var currentState = initialState

    override fun accept(intention: Wish) =
            actor(intention, currentState)
                    .map { reducer(currentState, it).also { currentState = it } }
                    .subscribe(states)
}

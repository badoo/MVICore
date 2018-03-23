package com.badoo.mvicore2.store

import com.badoo.mvicore2.store.internal.DefaultEngine
import io.reactivex.Observable

interface Engine<Wish : Any, State : Any> : Store<Wish, State> {

    interface Actor<in Wish : Any, in State : Any, Effect : Any> : (Wish, State) -> Observable<Effect>

    interface Reducer<State, in Effect> : (State, Effect) -> State

    companion object {

        fun <Wish : Any, State : Any, Effect : Any> create(
                initialState: State,
                actor: Actor<Wish, State, Effect>,
                reducer: Reducer<State, Effect>
        ): Engine<Wish, State> = DefaultEngine(initialState, actor, reducer)
    }
}

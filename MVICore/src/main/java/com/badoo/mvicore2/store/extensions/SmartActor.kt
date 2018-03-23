package com.badoo.mvicore2.store.extensions

import com.badoo.mvicore2.store.Engine
import com.badoo.mvicore2.store.internal.DefaultEngine

interface SmartActor<in Wish : Any, State : Any> : Engine.Actor<Wish, State, SmartActor.Effect<State>> {

    interface Effect<State> : (State) -> State

    fun <State> reducer() = object : Engine.Reducer<State, Effect<State>> {
        override fun invoke(state: State, effect: Effect<State>): State = effect(state)
    }
}

fun <Wish : Any, State : Any> Engine.Companion.create(
        initialState: State,
        actor: SmartActor<Wish, State>
): Engine<Wish, State> = DefaultEngine(initialState, actor, actor.reducer())

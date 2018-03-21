package com.badoo.mvicore2.store

import com.badoo.mvicore2.store.internal.DefaultReducer
import io.reactivex.Observable

interface Reducer<in Wish : Any, State : Any> : (Wish) -> Observable<State> {

    val currentState: State

    interface Effect<State : Any> : (State) -> State

    interface Actor<in Wish : Any, State : Any> : (Wish, State) -> Observable<Effect<State>>

    companion object {

        fun <Wish : Any, State : Any> create(initialState: State, actor: Actor<Wish, State>): Reducer<Wish, State> =
                DefaultReducer(initialState, actor)
    }
}

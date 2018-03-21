package com.badoo.mvicore2.store.internal

import com.badoo.mvicore2.store.Reducer
import com.badoo.mvicore2.store.Reducer.Actor
import io.reactivex.Observable

internal class DefaultReducer<in Wish : Any, State : Any>(
        initialState: State,
        private val actor: Actor<Wish, State>
) : Reducer<Wish, State> {

    override var currentState = initialState

    override fun invoke(intention: Wish): Observable<State> = actor(intention, currentState).map { it(currentState).also { currentState = it } }
}

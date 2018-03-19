package com.badoo.mvicore2

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class DefaultEngine<Wish : Any, State : Any>(
        reducer: DefaultReducer<Wish, State>,
//        the rest is pre-initialised and normally should be overridden only in tests
        intentions: PublishSubject<Wish> = PublishSubject.create<Wish>(),
        states: Observable<State> = intentions.flatMap { reducer(it) }
                .replay(1)
                .autoConnect(0)
                .distinctUntilChanged()
) : Store<State, Wish>, Observer<Wish> by intentions, ObservableSource<State> by states {

    constructor (state: State, actor: Actor<Wish, State>) : this(DefaultReducer(state, actor))
}

open class DefaultReducer<in Intention, State>(
        var state: State,
        private val actor: Actor<Intention, State>
) : (Intention) -> Observable<State> {
    override fun invoke(intention: Intention): Observable<State> = actor(intention, state).map { it(state).also { state = it } }
}


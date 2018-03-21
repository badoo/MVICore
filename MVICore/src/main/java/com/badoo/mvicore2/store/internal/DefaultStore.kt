package com.badoo.mvicore2.store.internal

import com.badoo.mvicore2.store.Reducer
import com.badoo.mvicore2.store.Store
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

internal class DefaultStore<Wish : Any, State : Any>(
        reducer: Reducer<Wish, State>,
//        the rest is pre-initialised and normally should be overridden only in tests
        wishes: PublishSubject<Wish> = PublishSubject.create<Wish>(),
        states: Observable<State> = wishes.flatMap { reducer(it) }
                .startWith(reducer.currentState)
                .replay(1)
                .autoConnect(0)
                .distinctUntilChanged()
) : Store<Wish, State>, Observer<Wish> by wishes, ObservableSource<State> by states

package com.badoo.mvicore2.store.internal

import com.badoo.mvicore2.binder.Processor
import com.badoo.mvicore2.store.Engine
import com.badoo.mvicore2.store.Engine.Actor
import com.badoo.mvicore2.store.Engine.News
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject

internal class DefaultEngine<Wish : Any, State : Any, Effect : Any>(
        initialState: State,
        private val actor: Actor<Wish, State, Effect>,
        private val reducer: Engine.Reducer<State, Effect>,
        // adding the News interface to an Effect is a hack, we should expose only public data
        private val news: Processor<Effect, News>,
        private val states: BehaviorSubject<State> = BehaviorSubject.createDefault(initialState),
        private val disposable: CompositeDisposable = CompositeDisposable()
) : Engine<Wish, State>,
        ObservableSource<State> by states,
        Disposable by disposable {

    override val currentState: State get() = states.value

    override fun accept(intention: Wish) {
        disposable += actor(intention, states.value)
                .subscribe { effect ->
                    //  if effect implements (State) -> State we can get rid of reducer:
                    //  effect(states.value).let { states.onNext(it) }
                    reducer(states.value, effect).let { states.onNext(it) }
                    news.accept(effect)
                }
    }
}

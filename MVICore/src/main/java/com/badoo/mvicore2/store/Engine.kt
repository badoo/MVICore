package com.badoo.mvicore2.store

import com.badoo.mvicore2.binder.Processor
import com.badoo.mvicore2.binder.Transformer
import com.badoo.mvicore2.store.internal.DefaultEngine
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface Engine<Wish : Any, State : Any> : Store<Wish, State>, Disposable {

    interface Actor<in Wish : Any, in State : Any, Effect : Any> : (Wish, State) -> Observable<Effect>

    interface Reducer<State, in Effect> : (State, Effect) -> State

    interface News

    companion object {

        fun <Wish : Any, State : Any, Effect : Any> create(
                initialState: State,
                actor: Actor<Wish, State, Effect>,
                reducer: Reducer<State, Effect>,
                newsTransformer: Transformer<Effect, News>
        ): Engine<Wish, State> =
                DefaultEngine(initialState, actor, reducer, Processor.mapping(newsTransformer))
    }
}

package com.badoo.mvicore.rx.feature

import com.badoo.mvicore.rx.element.Actor
import com.badoo.mvicore.rx.element.Bootstrapper
import com.badoo.mvicore.rx.element.NewsPublisher
import com.badoo.mvicore.rx.element.Reducer
import io.reactivex.Observable

open class ReducerFeature<Wish : Any, State : Any, News : Any>(
    initialState: State,
    bootstrapper: Bootstrapper<Wish>? = null,
    reducer: Reducer<State, Wish>,
    newsPublisher: SimpleNewsPublisher<Wish, State, News>? = null
) : ActorReducerFeature<Wish, Wish, State, News>(
    initialState = initialState,
    actor = PassthroughActor(),
    bootstrapper = bootstrapper,
    reducer = reducer,
    newsPublisher = newsPublisher?.let { NoEffectNewsPublisher(it) }
) {
    private class PassthroughActor<State : Any, Wish : Any> : Actor<State, Wish, Wish> {
        override fun invoke(state: State, wish: Wish): Observable<out Wish> =
            Observable.just(wish)
    }

    private class NoEffectNewsPublisher<Wish : Any, State : Any, News: Any>(
        private val simpleNewsPublisher: SimpleNewsPublisher<Wish, State, News>
    ) : NewsPublisher<Wish, Wish, State, News> {
        override fun invoke(old: State, action: Wish, effect: Wish, new: State): News? =
            simpleNewsPublisher(old, action, new)
    }
}

typealias SimpleNewsPublisher<Wish, State, News> = (old: State, wish: Wish, state: State) -> News?

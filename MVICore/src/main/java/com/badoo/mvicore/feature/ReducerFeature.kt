package com.badoo.mvicore.featurewithaction.implementation

import com.badoo.mvicore.feature.DefaultFeature
import io.reactivex.Observable
import io.reactivex.Observable.just

abstract class ReducerFeature<Wish : Any, State : Any>(
    initialState: State,
    reducer: Reducer<State, Wish>
) : DefaultFeature<Wish, Wish, Wish, State>(
    initialState = initialState,
    wishToAction = { wish -> wish },
    actor = BypassActor(),
    reducer = reducer
) {
    class BypassActor<State : Any, Wish : Any> : DefaultFeature.Actor<State, Wish, Wish> {
        override fun invoke(state: State, wish: Wish): Observable<Wish> =
            just(wish)
    }
}

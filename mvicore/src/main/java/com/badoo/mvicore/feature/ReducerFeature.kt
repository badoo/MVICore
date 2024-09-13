package com.badoo.mvicore.feature

import com.badoo.binder.middleware.config.NonWrappable
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.just

/**
 * An implementation of a single threaded feature.
 *
 * Please be aware of the following threading behaviours based on whether a 'featureScheduler' is provided.
 *
 * No 'featureScheduler' provided:
 * The feature must execute on the thread that created the class. If the bootstrapper/actor observables
 * change to a different thread it is your responsibility to switch back to the feature's original
 * thread via observeOn, otherwise an exception will be thrown.
 *
 * 'featureScheduler' provided (this must be single threaded):
 * The feature does not have to execute on the thread that created the class. It automatically
 * switches to the feature scheduler thread when necessary.
 */
open class ReducerFeature<Wish : Any, State : Any, News : Any>(
    initialState: State,
    reducer: Reducer<State, Wish>,
    bootstrapper: Bootstrapper<Wish>? = null,
    newsPublisher: SimpleNewsPublisher<Wish, State, News>? = null,
    featureScheduler: FeatureScheduler? = null
) : BaseFeature<Wish, Wish, Wish, State, News>(
    initialState = initialState,
    bootstrapper = bootstrapper,
    wishToAction = { wish -> wish },
    actor = BypassActor(),
    reducer = reducer,
    newsPublisher = newsPublisher,
    featureScheduler = featureScheduler
) {
    class BypassActor<in State : Any, Wish : Any> : Actor<State, Wish, Wish>, NonWrappable {
        override fun invoke(state: State, wish: Wish): Observable<Wish> =
            just(wish)
    }

    abstract class SimpleNewsPublisher<in Wish : Any, in State : Any, out News : Any> : NewsPublisher<Wish, Wish, State, News> {
        override fun invoke(wish: Wish, effect: Wish, state: State): News? =
            invoke(wish, state)

        abstract fun invoke(wish: Wish, state: State): News?
    }
}

package com.badoo.mvicore.core

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Middleware
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import io.reactivex.Observable
import io.reactivex.Observer

/**
 * TODO
 */
abstract class Feature<State : Any, Wish : Any, Effect : Any>(
    val engine: Engine<State, Wish, Effect>,
    val initialState: State,
    val initialSources: List<Observable<Wish>>? = null,
    val actor: Actor<Wish, State, Effect>,
    val reducer: Reducer<State, Effect>,
    val middlewares: List<Middleware<State, Effect>>? = null,
    val newsObserver: Observer<News>? = null
) : Store<State, Wish> by engine {
    init {
        engine.init(this)
    }
}

package com.badoo.mvicore.common.feature

//open class ReducerFeature<Wish : Any, State : Any, News : Any>(
//    initialState: State,
//    bootstrapper: Bootstrapper<Wish>? = null,
//    private val reducer: Reducer<State, Wish>,
//    private val newsPublisher: SimpleNewsPublisher<State, Wish, News>? = null
//) : ActorReducerFeature<Wish, Wish, State, News>(
//    actor = {  }
//)

typealias SimpleNewsPublisher<State, Wish, News> = (old: State, wish: Wish, state: State) -> News?

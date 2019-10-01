//package com.badoo.mvicore.common.element
//
//import com.badoo.mvicore.common.Cancellable
//import com.badoo.mvicore.common.Sink
//import com.badoo.mvicore.common.Source
//import com.badoo.mvicore.common.source
//import kotlin.contracts.Effect
//
//class ActorReducerFeature<Wish : Any, State : Any, Effect : Any, News : Any>(
//    initialState: State,
//    bootstrapper: Bootstrapper<Wish>? = null,
//    private val actor: Actor<State, Wish, Effect>,
//    private val reducer: Reducer<State, Effect>,
//    private val newsPublisher: SimpleNewsPublisher<State, Wish, News>? = null
//) : Feature<Wish, State, News> {
//    private val newsSource = source<News>()
//    private val stateSource = source(initialState)
//
//    init {
//        bootstrapper?.invoke()?.connect(this)
//    }
//
//    override fun invoke(wish: Wish) {
//        process(wish)
//    }
//
//    override fun connect(sink: Sink<State>): Cancellable = stateSource.connect(sink)
//
//    override val news: Source<News> = newsSource
//
//    private fun process(wish: Wish) {
//        val oldState = stateSource.value ?: return
//        val newState = reducer(oldState, wish)
//        stateSource.invoke(newState)
//        newsPublisher?.invoke(oldState, wish, newState)?.let(newsSource)
//    }
//}

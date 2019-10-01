//package com.badoo.mvicore.common.element
//
//import com.badoo.mvicore.common.Cancellable
//import com.badoo.mvicore.common.Sink
//import com.badoo.mvicore.common.Source
//import com.badoo.mvicore.common.source
//
//abstract class BaseFeature<Action, Wish, Effect, State, News> (
//    initialState: State,
//    private val bootstrapper: Bootstrapper<Action>? = null,
//    private val wishToAction: (Wish) -> Action,
//    private val actor: Actor<State, Action, Effect>,
//    private val reducer: Reducer<State, Effect>,
//    private val newsPublisher: NewsPublisher<Action, State, Effect, News>? = null
//) : Feature<Wish, State, News> {
//    private val stateSource = source(initialState)
//    private val newsSource = source<News>()
//
//    init {
//        bootstrapper?.invoke()?.connect()
//    }
//
//    override fun invoke(wish: Wish) {
//        val oldState = state
//        actor.invoke(oldState, wishToAction(wish))
//            .connect { effect ->
//                val newState = reducer(this.state, effect)
//                stateSource(newState)
//                newsPublisher(wish, effect, newState)?.let {
//                    newsSource(it)
//                }
//            }
//    }
//
//    override fun connect(sink: Sink<State>) = stateSource.connect(sink)
//
//    override fun cancel() {
//
//    }
//
//    val state: State
//        get() = stateSource.value!!
//
//    override val news: Source<News>
//        get() = newsSource
//}

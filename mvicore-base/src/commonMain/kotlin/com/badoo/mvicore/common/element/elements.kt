package com.badoo.mvicore.common.element

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Sink
import com.badoo.mvicore.common.Source

typealias Bootstrapper<Action> = () -> Source<out Action>

typealias Actor<State, Wish, Effect> = (state: State, wish: Wish) -> Source<out Effect>

typealias Reducer<State, Effect> = (state: State, effect: Effect) -> State

typealias NewsPublisher<Wish, State, Effect, News> = (old: State, wish: Wish, effect: Effect, new: State) -> News?

interface Feature<Wish, State, News>: Sink<Wish>, Source<State>, Cancellable {
    val news: Source<News>
}

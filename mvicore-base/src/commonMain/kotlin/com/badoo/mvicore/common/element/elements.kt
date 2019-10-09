package com.badoo.mvicore.common.element

import com.badoo.mvicore.common.Cancellable
import com.badoo.mvicore.common.Source

typealias Bootstrapper<Action> = () -> Source<out Action>

typealias Actor<State, Wish, Effect> = (state: State, wish: Wish) -> Source<out Effect>

typealias Reducer<State, Effect> = (state: State, effect: Effect) -> State

typealias NewsPublisher<Action, Effect, State, News> = (old: State, action: Action, effect: Effect, new: State) -> News?

typealias PostProcessor<Action, Effect, State> = (old: State, action: Action, effect: Effect, new: State) -> Action?

interface Feature<Wish, State, News>: (Wish) -> Unit, Source<State>, Cancellable {
    val news: Source<News>
}

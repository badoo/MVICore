package com.badoo.mvicore.rx.element

import com.badoo.mvicore.common.element.NewsPublisher
import com.badoo.mvicore.common.element.PostProcessor
import com.badoo.mvicore.common.element.Reducer
import io.reactivex.ObservableSource

interface Bootstrapper<out Action> {
    operator fun invoke(): ObservableSource<out Action>
}

interface Actor<in State, in Action, out Effect> {
    operator fun invoke(state: State, action: Action): ObservableSource<out Effect>
}

typealias Reducer<State, Effect> = Reducer<State, Effect>

typealias NewsPublisher<Action, Effect, State, News> = NewsPublisher<Action, Effect, State, News>

typealias PostProcessor<Action, Effect, State> = PostProcessor<Action, Effect, State>

typealias SimpleNewsPublisher<Wish, State, News> = (old: State, wish: Wish, state: State) -> News?

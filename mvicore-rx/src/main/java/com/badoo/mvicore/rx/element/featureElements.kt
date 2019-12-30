package com.badoo.mvicore.rx.element

import io.reactivex.ObservableSource

interface Bootstrapper<Action> {
    operator fun invoke(): ObservableSource<Action>
}

interface Actor<State, Action, Effect> {
    operator fun invoke(state: State, action: Action): ObservableSource<out Effect>
}

interface Reducer<State, Effect> {
    operator fun invoke(state: State, effect: Effect): State
}

interface NewsPublisher<Action, Effect, State, News> {
    operator fun invoke(old: State, action: Action, effect: Effect, new: State): News?
}

interface PostProcessor<Action, Effect, State> {
    operator fun invoke(old: State, action: Action, effect: Effect, new: State): Action?
}

package com.badoo.mvicore.common.element

import com.badoo.mvicore.common.Source

interface Bootstrapper<Action> {
    operator fun invoke(): Source<Action>
}

interface Actor<State, Action, Effect> {
    operator fun invoke(state: State, action: Action): Source<out Effect>
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

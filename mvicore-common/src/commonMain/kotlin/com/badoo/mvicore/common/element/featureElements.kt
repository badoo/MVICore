package com.badoo.mvicore.common.element

import com.badoo.mvicore.common.Source

interface Bootstrapper<out Action> {
    operator fun invoke(): Source<Action>
}

interface Actor<in State, in Action, out Effect> {
    operator fun invoke(state: State, action: Action): Source<Effect>
}

interface Reducer<State, in Effect> {
    operator fun invoke(state: State, effect: Effect): State
}

interface NewsPublisher<in Action, in Effect, in State, out News> {
    operator fun invoke(old: State, action: Action, effect: Effect, new: State): News?
}

interface PostProcessor<Action, in Effect, in State> {
    operator fun invoke(old: State, action: Action, effect: Effect, new: State): Action?
}

package com.badoo.mvicore.element

typealias NewsPublisher<Action, Effect, State, News> =
    (action: Action, effect: Effect, state: State) -> News?

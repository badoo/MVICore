package com.badoo.mvicore.element

typealias PostProcessor<Action, Effect, State> =
    (action: Action, effect: Effect, state: State) -> Action?


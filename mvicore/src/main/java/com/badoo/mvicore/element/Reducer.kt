package com.badoo.mvicore.element

typealias Reducer<State, Effect> =
    (state: State, effect: Effect) -> State

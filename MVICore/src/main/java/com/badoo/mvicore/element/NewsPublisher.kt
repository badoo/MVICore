package com.badoo.mvicore.element

typealias NewsPublisher<Action, Effect, State, News> = (Action, Effect, State) -> News?

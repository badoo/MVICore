package com.badoo.mvicore.element

typealias PostProcessor<Action, Effect, State> = (Action, Effect, State) -> Action?

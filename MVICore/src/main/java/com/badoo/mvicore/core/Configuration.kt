package com.badoo.mvicore.core

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Middleware
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer

/**
 * Represents a configuration for [Feature] implementations
 */
open class Configuration<State : Any, Wish : Any, Effect : Any>(
    val initialState: State,
    val bootstrapper: Bootstrapper<Wish>? = null,
    val actor: Actor<Wish, State, Effect>,
    val reducer: Reducer<State, Effect>,
    val middlewares: List<Middleware<State, Effect>>? = null,
    val newsPublisher: ((News) -> Unit)? = null
)

package com.badoo.mvicore.element

/**
 * TODO
 */
interface Middleware<State : Any, Effect : Any> {

    fun create(nextReducer: Reducer<State, Effect>): Reducer<State, Effect>
}

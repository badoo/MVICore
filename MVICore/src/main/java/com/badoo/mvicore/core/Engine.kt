package com.badoo.mvicore.core

/**
 * TODO
 */
interface Engine<State : Any, Wish : Any, Effect : Any> : Store<State, Wish> {

    fun init(feature: Feature<State, Wish, Effect>)
}

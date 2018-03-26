package com.badoo.mvicore.element

interface Store<Wish : Any, State : Any> : Processor<Wish, State> {

    val state: State
}

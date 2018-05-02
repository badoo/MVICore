package com.badoo.mvicore2.store

import com.badoo.mvicore2.binder.Processor

interface Store<Wish : Any, State : Any> : Processor<Wish, State>{

    val currentState : State
}


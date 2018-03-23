package com.badoo.mvicore2.store

import com.badoo.mvicore2.binder.Bindable

interface Store<Wish : Any, State : Any> : Bindable<Wish, State>{

    val currentState : State
}


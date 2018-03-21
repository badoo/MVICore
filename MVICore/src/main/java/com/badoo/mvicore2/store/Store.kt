package com.badoo.mvicore2.store

import com.badoo.mvicore2.binder.Bindable
import com.badoo.mvicore2.store.internal.DefaultStore

interface Store<Wish : Any, State : Any> : Bindable<Wish, State> {

    companion object {

        fun <Wish : Any, State : Any> create(initialState: State, actor: Reducer.Actor<Wish, State>): Store<Wish, State> =
                DefaultStore(Reducer.create(initialState, actor))

        fun <Wish : Any, State : Any> create(reducer: Reducer<Wish, State>): Store<Wish, State> = DefaultStore(reducer)
    }

}


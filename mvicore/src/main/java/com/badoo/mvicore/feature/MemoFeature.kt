package com.badoo.mvicore.feature

open class MemoFeature<State : Any>(
    initialState: State
) : Feature<State, State, Nothing> by ReducerFeature<State, State, Nothing>(
    initialState = initialState,
    reducer = { _, effect -> effect }
)

package com.badoo.mvicore.core

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * TODO
 */
interface Store<State : Any, Wish : Any> : Disposable {

    /**
     * Current state, can be accessed from any thread
     */
    val state: State

    /**
     * Observable stream of states, emits on the main thread
     */
    val states: Observable<State>

    /**
     * Connects an observable source of wishes to the feature which can trigger new state emissions
     */
    fun connectSource(source: Observable<Wish>)

    /**
     * Disconnects a source, after which that source can no longer trigger new state emissions
     */
    fun disconnectSource(source: Observable<Wish>)
}

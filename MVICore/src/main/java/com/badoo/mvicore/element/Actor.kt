package com.badoo.mvicore.element

import android.support.annotation.MainThread
import io.reactivex.disposables.Disposable

abstract class Actor<in Wish : Any, State : Any, Effect : Any> {

    private lateinit var getState: () -> State
    private lateinit var dispatch: (Effect) -> Unit
    private var isInitialized: Boolean = false

    /**
     * Provides access to current state of Feature, must be accessed only from Main thread
     */
    protected val state: State
        get() = getState()

    /**
     * Invoked by Feature for every Wish
     *
     * @param wish a Wish
     * @return Disposable if there are any background operations for the Wish, null otherwise
     */
    @MainThread
    abstract operator fun invoke(wish: Wish): Disposable?

    /**
     * Initializes the Actor. Called by Feature, do not call manually.
     */
    fun init(getState: () -> State, dispatch: (Effect) -> Unit) {
        if (isInitialized) {
            throw RuntimeException("Feature is already initialized: $this")
        }

        this.getState = getState
        this.dispatch = dispatch
        isInitialized = true
    }

    /**
     * Dispatches Effect to Feature where it will be passed to Reducer. You can get a new Feature right after this method returns.
     * Must be called only on Main thread.
     *
     * @param effect an Effect to dispatch
     * @return Always null, this is for your convenience to return from Actor
     */
    @MainThread
    protected fun dispatch(effect: Effect): Disposable? {
        dispatch.invoke(effect)
        return null
    }
}

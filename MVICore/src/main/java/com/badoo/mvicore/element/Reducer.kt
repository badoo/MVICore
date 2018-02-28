package com.badoo.mvicore.element

import android.support.annotation.MainThread

/**
 * Reducers are used to create a new [State] by applying an [Effect] to the current one.
 * Reducer should be called on the main thread.
 *
 * @param State  type of Feature's State
 * @param Effect type of Feature's Effects
 */
interface Reducer<State : Any, in Effect : Any> {
    /**
     * @param state  current State
     * @param effect Effect that should be applied
     * @return new State
     */
    @MainThread
    operator fun invoke(state: State, effect: Effect): State
}

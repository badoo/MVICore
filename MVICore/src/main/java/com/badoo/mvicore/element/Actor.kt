package com.badoo.mvicore.element

import android.support.annotation.MainThread
import io.reactivex.Observable

/**
 * Actors are used to convert Wishes into Effects.
 * Each Effect will then be converted by [Reducer] into a new State.
 */
interface Actor<in Wish : Any, in State : Any, Effect : Any> {

    /**
     * Converts Wishes into Effects. Called on the main thread.
     * Effects must also be published on the main thread.
     *
     * @param wish a Wish, call to action
     * @param state current state of the Feature
     * @return see [Effects]
     */
    @MainThread
    operator fun invoke(wish: Wish, state: State): Observable<Effect>

    companion object {
        /**
         * Helper method to produce multiple [Effect]s
         */
        fun <Effect : Any> combineEffects(immediate: Effect?, additional: Observable<Effect> = Observable.empty()): Observable<Effect> =
            immediate
                ?.let { Observable.just(it) }
                ?.concatWith(additional)
                ?: additional
    }
}

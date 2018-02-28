package com.badoo.mvicore.core

import android.support.annotation.MainThread
import com.badoo.mvicore.element.News
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Low level interface for Features
 *
 * @param State type of Feature's State. It is advised that Features' states are immutable.
 * @param Wish  type of Feature's Wishes
 */
interface Feature<State : Any, in Wish : Any> : Disposable {

    /**
     * Current state, can be accessed from any thread
     */
    val state: State

    /**
     * Observable of States, emissions are performed on Main thread
     */
    val states: Observable<State>

    /**
     * Notifies the Feature about Wishes, must only be called from Main thread
     */
    @MainThread
    fun onWish(wish: Wish)
}

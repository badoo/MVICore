package com.badoo.mvicore.element

import android.support.annotation.MainThread
import io.reactivex.disposables.Disposable

interface Actor<in Wish : Any, in State : Any, out Effect : Any> {
    @MainThread
    operator fun invoke(wish: Wish, state: State, produce: (Effect) -> State): Disposable?
}

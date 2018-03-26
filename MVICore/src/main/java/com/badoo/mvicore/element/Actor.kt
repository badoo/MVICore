package com.badoo.mvicore.element

import android.support.annotation.MainThread
import io.reactivex.Observable

interface Actor<in Wish : Any, in State : Any, Effect : Any> {
    @MainThread
    operator fun invoke(wish: Wish, state: State): Observable<Effect>
}

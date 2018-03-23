package com.badoo.mvicore.element

import android.support.annotation.MainThread

interface Reducer<State : Any, in Effect : Any> {
    @MainThread
    operator fun invoke(state: State, effect: Effect): State
}

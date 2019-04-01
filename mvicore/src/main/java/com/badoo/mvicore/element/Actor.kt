package com.badoo.mvicore.element

import io.reactivex.Observable

typealias Actor<State, Action, Effect> =
    (state: State, action: Action) -> Observable<out Effect>

package com.badoo.mvicore.element

import io.reactivex.rxjava3.core.Observable

typealias Actor<State, Action, Effect> =
    (state: State, action: Action) -> Observable<out Effect>

package com.badoo.mvicore.element

import io.reactivex.Observable

typealias Actor<State, Action, Effect> = (State, Action) -> Observable<out Effect>

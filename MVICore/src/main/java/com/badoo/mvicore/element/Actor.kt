package com.badoo.mvicore.element

import io.reactivex.Observable

typealias Actor<State, Wish, Effect> = (State, Wish) -> Observable<Effect>

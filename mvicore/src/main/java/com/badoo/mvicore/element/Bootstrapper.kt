package com.badoo.mvicore.element

import io.reactivex.Observable

typealias Bootstrapper<Action> = () -> Observable<out Action>

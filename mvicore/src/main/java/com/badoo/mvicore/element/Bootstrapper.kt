package com.badoo.mvicore.element

import io.reactivex.rxjava3.core.Observable

typealias Bootstrapper<Action> = () -> Observable<out Action>

package com.badoo.mvicore.element

interface TimeCapsule<in T : Any> {

    operator fun <State : T> get(key: Any): State?

    fun <State : T> register(key: Any, stateSupplier: () -> State)
}

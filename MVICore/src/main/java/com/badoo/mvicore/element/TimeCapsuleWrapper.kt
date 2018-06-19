package com.badoo.mvicore.element

class TimeCapsuleWrapper<T : Any>(
    private val capsule: TimeCapsule<T>,
    private val suffix: String
) : TimeCapsule<T> {

    private fun generateKey(key: Any) =
        "$key$suffix"

    override fun <State : T> get(key: Any): State? =
        capsule[generateKey(key)]

    override fun <State : T> register(key: Any, stateSupplier: () -> State) =
        capsule.register(generateKey(key), stateSupplier)
}

fun <T : Any> TimeCapsule<T>.wrap(suffix: String) =
    TimeCapsuleWrapper(this, suffix)

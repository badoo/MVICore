package com.badoo.mvicore.element

interface TimeCapsule<in T : Any> {

    operator fun <State : T> get(key: Any): State?

    fun <State : T> register(key: Any, stateSupplier: () -> State)
}

fun <T : Any> TimeCapsule<T>.wrap(suffix: String) =
    object : TimeCapsule<T> {

        private fun generateKey(key: Any) =
            "$key$suffix"

        override fun <State : T> get(key: Any): State? =
            this@wrap[generateKey(key)]

        override fun <State : T> register(key: Any, stateSupplier: () -> State) =
            this@wrap.register(generateKey(key), stateSupplier)
    }

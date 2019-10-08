package com.badoo.mvicore.util

sealed class SealedModel {
    abstract val list: List<String>

    data class Value(
        val int: Int = 0,
        override val list: List<String> = emptyList(),
        val nullable: Boolean? = null
    ): SealedModel()

    object Nothing: SealedModel() {
        override val list: List<String> = emptyList()
    }
}

sealed class Nested {
    sealed class SubNested : Nested() {
        data class Value(val list: List<String>) : SubNested()
        object Nothing : SubNested()
    }
    object Something: Nested()
}

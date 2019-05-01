package com.badoo.mvicore.plugin.model

data class Id(
    val name: String,
    val hash: String?
) {
    val shortName
        get() = name.split(".").last { it.isNotEmpty() }
}

data class Connection(
    val from: Id?,
    val to: Id?,
    val name: String?
) {
    override fun toString(): String =
        name ?: "${from?.shortName.orEmpty()} -> ${to?.shortName.orEmpty()}"
}
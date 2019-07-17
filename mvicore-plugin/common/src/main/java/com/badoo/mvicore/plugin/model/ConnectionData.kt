package com.badoo.mvicore.plugin.model

data class ConnectionData(
    val from: Id?,
    val to: Id?,
    val name: String?
) {
    override fun toString(): String =
        name ?: "${from?.shortName} -> ${to?.shortName}"
}

data class Id(
    val name: String,
    val hash: String
) {
    val shortName
        get() = name.className()
}

private fun String.className() =
    split('.')
        .takeLastWhile { it.indexOfFirst { it.isUpperCase() } == 0 }
        .joinToString(separator = ".")

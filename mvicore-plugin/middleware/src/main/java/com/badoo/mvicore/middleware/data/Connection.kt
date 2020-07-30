package com.badoo.mvicore.middleware.data

import com.badoo.binder.Connection

fun Connection<out Any, out Any>.parse() =
    ConnectionData(
        Id(from.nameString(), from.hashString()),
        Id(to.nameString(), to.hashString()),
        name
    )

private fun Any?.nameString() = toString().split('@').first()
private fun Any?.hashString() = Integer.toHexString(this.hashCode())

package com.badoo.mvicore.middleware.data

import com.badoo.binder.Connection
import com.badoo.mvicore.plugin.model.ConnectionData
import com.badoo.mvicore.plugin.model.Id

fun Connection<out Any, out Any>.parse() =
    ConnectionData(
        Id(from.nameString(), from.hashString()),
        Id(to.nameString(), to.hashString()),
        name
    )

private fun Any?.nameString() = toString().split('@').first()
private fun Any?.hashString() = Integer.toHexString(this.hashCode())

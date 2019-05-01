package com.badoo.mvicore.plugin.model

import com.google.gson.JsonElement

data class Item(
    val connection: Connection?,
    val element: JsonElement
)
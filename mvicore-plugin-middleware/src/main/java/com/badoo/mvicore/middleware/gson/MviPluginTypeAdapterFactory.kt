package com.badoo.mvicore.middleware.gson

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal class MviPluginTypeAdapterFactory : TypeAdapterFactory {

    private fun <T> Gson.getDelegate(typeToken: TypeToken<T>) =
        getDelegateAdapter(this@MviPluginTypeAdapterFactory, typeToken)

    override fun <R : Any> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R> =
        object : TypeAdapter<R>() {
            override fun read(reader: JsonReader): R =
                gson.getDelegate(type)
                    .fromJsonTree(Streams.parse(reader))

            override fun write(out: JsonWriter, value: R) {
                val srcType = value.javaClass
                val label = srcType.canonicalName
                val delegate = gson.getDelegate(TypeToken.get(srcType))
                val jsonObject = delegate.toJsonTree(value).let {
                    if (it.isJsonObject) {
                        it.asJsonObject
                    } else {
                        JsonObject().apply {
                            add(VALUE_FIELD, it)
                        }
                    }
                }
                jsonObject.add(TYPE_FIELD, JsonPrimitive(label))
                jsonObject.add(TIMESTAMP_FIELD, JsonPrimitive(System.currentTimeMillis()))
                Streams.write(jsonObject, out)
            }
        }.nullSafe()

    companion object {
        private const val TYPE_FIELD = "\$type"
        private const val VALUE_FIELD = "\$value"
        private const val TIMESTAMP_FIELD = "\$timestamp"
    }
}

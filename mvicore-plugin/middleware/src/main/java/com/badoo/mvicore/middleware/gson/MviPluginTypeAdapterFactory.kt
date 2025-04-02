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
import java.util.IdentityHashMap
import kotlin.concurrent.getOrSet

internal class MviPluginTypeAdapterFactory(
    private val ignoreValues: (Any?) -> Boolean
) : TypeAdapterFactory {

    private val map: ThreadLocal<IdentityHashMap<Any, Any>> = ThreadLocal()

    private fun <T> Gson.getDelegate(typeToken: TypeToken<T>) =
        getDelegateAdapter(
            this@MviPluginTypeAdapterFactory,
            typeToken
        )

    override fun <R : Any> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R> =
        object : TypeAdapter<R>() {
            override fun read(reader: JsonReader): R =
                gson.getDelegate(type)
                    .fromJsonTree(Streams.parse(reader))

            override fun write(out: JsonWriter, value: R?) {
                when (value) {
                    null -> out.nullValue()
                    else -> writeObj(out, value)
                }
            }

            private fun writeObj(out: JsonWriter, value: R) {
                val values = map.getOrSet { IdentityHashMap() }

                val srcType = value.javaClass
                val label = srcType.name
                val delegate = gson.getDelegate(TypeToken.get(srcType))
                val jsonObject = when {
                    value is Class<*> -> JsonPrimitive(value.name)
                    ignoreValues(value) -> JsonPrimitive(value.toString())
                    values.containsKey(value) -> JsonPrimitive("Recursive: $label")
                    else -> {
                        values[value] = value
                        val tree = delegate.toJsonTree(value)
                        values.remove(value)
                        tree
                    }
                }.let {
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
        }

    companion object {
        private const val TYPE_FIELD = "\$type"
        private const val VALUE_FIELD = "\$value"
        private const val TIMESTAMP_FIELD = "\$timestamp"
    }
}

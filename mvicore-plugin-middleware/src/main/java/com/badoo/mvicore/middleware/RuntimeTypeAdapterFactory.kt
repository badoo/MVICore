package com.badoo.mvicore.middleware

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.util.LinkedHashMap

class RuntimeTypeAdapterFactory(
    private val typeFieldName: String,
    private val maintainType: Boolean
) : TypeAdapterFactory {
    private val labelToSubtype = LinkedHashMap<String, Class<*>>()

    override fun <R: Any> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R>? {
        val labelToDelegate = LinkedHashMap<String, TypeAdapter<*>>()
        val subtypeToDelegate = LinkedHashMap<Class<*>, TypeAdapter<*>>()
        for ((key, value) in labelToSubtype) {
            val delegate = gson.getDelegateAdapter(this, TypeToken.get(value))
            labelToDelegate[key] = delegate
            subtypeToDelegate[value] = delegate
        }

        return object : TypeAdapter<R>() {
            @Throws(IOException::class)
            override fun read(`in`: JsonReader): R {
                val jsonElement = Streams.parse(`in`)
                val labelJsonElement: JsonElement?
                if (maintainType) {
                    labelJsonElement = jsonElement.asJsonObject.get(typeFieldName)
                } else {
                    labelJsonElement = jsonElement.asJsonObject.remove(typeFieldName)
                }

                val label = labelJsonElement.asString
                val delegate = labelToDelegate[label] as TypeAdapter<R>
                return delegate.fromJsonTree(jsonElement)
            }

            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: R) {
                val srcType = value.javaClass
                val label = srcType.canonicalName
                val delegate = gson.getDelegateAdapter(this@RuntimeTypeAdapterFactory, TypeToken.get(srcType))
                val jsonObject = delegate.toJsonTree(value).let {
                    if (it.isJsonObject) {
                        it.asJsonObject
                    } else {
                        JsonObject().apply {
                            add("\$value", it)
                        }
                    }
                }


                if (maintainType) {
                    Streams.write(jsonObject, out)
                    return
                }

                val clone = JsonObject()

                if (jsonObject.has(typeFieldName)) {
                    throw JsonParseException("cannot serialize " + srcType.getName()
                        + " because it already defines a field named " + typeFieldName)
                }
                clone.add(typeFieldName, JsonPrimitive(label))

                for ((key, value1) in jsonObject.entrySet()) {
                    clone.add(key, value1)
                }
                Streams.write(clone, out)
            }
        }.nullSafe()
    }
}

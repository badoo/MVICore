package com.badoo.mvicore.middleware

import com.google.gson.*
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

import java.io.IOException
import java.util.LinkedHashMap

    class RuntimeTypeAdapterFactory<T: Any> private constructor(
    private val typeFieldName: String,
    private val maintainType: Boolean
) : TypeAdapterFactory {
    private val labelToSubtype = LinkedHashMap<String, Class<*>>()
    private val subtypeToLabel = LinkedHashMap<Class<*>, String>()

    /**
     * Registers `type` identified by `label`. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either `type` or `label`
     * have already been registered on this type adapter.
     */
    @JvmOverloads
    fun registerSubtype(type: Class<out T>, label: String? = type.simpleName): RuntimeTypeAdapterFactory<T> {
        if (label == null) {
            throw NullPointerException()
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw IllegalArgumentException("types and labels must be unique")
        }
        labelToSubtype[label] = type
        subtypeToLabel[type] = label
        return this
    }

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
                            add("value", it)
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

    companion object {

        /**
         * Creates a new runtime type adapter using for `baseType` using `typeFieldName` as the type field name. Type field names are case sensitive.
         * `maintainType` flag decide if the type will be stored in pojo or not.
         */
        fun <T: Any> of(baseType: Class<T>, typeFieldName: String, maintainType: Boolean): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(typeFieldName, maintainType)
        }

        /**
         * Creates a new runtime type adapter using for `baseType` using `typeFieldName` as the type field name. Type field names are case sensitive.
         */
        fun <T: Any> of(baseType: Class<T>, typeFieldName: String): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory(typeFieldName, false)
        }

        /**
         * Creates a new runtime type adapter for `baseType` using `"type"` as
         * the type field name.
         */
        fun <T: Any> of(baseType: Class<T>): RuntimeTypeAdapterFactory<T> {
            return RuntimeTypeAdapterFactory("\$type", false)
        }
    }
}
package com.badoo.mvicore.middleware.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

class SuperclassExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipClass(cls: Class<*>): Boolean = false

    override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
        val fieldName = fieldAttributes.name
        val theClass = fieldAttributes.declaringClass

        return isFieldInSuperclass(theClass, fieldName)
    }

    private fun isFieldInSuperclass(subclass: Class<*>, fieldName: String): Boolean {
        var superclass: Class<*>? = subclass.superclass
        while (superclass != null) {
            val hasField = superclass.declaredFields.any { it.name == fieldName }
            if (hasField) return true

            superclass = superclass.superclass
        }

        return false
    }
}

package com.badoo.mvicore.consumer.middlewareconfig

import io.reactivex.functions.Consumer

interface WrappingCondition {

    fun shouldWrap(consumer: Consumer<*>) : Boolean

    object Always : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>) =
            true
    }

    object Never : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>) =
            false
    }

    class Conditional(
        private val condition: () -> Boolean
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>) =
            condition()
    }

    class InstanceOf(
        private val clz: Class<*>
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>): Boolean =
            clz.isInstance(consumer)
    }

    sealed class PackageName {
        class SimpleMatcher(private val packageName: String) :
            WrappingCondition {
            override fun shouldWrap(consumer: Consumer<*>): Boolean =
                consumer::class.java.canonicalName.indexOf(packageName) != -1
        }

        class RegexMatcher(private val packageName: String) :
            WrappingCondition {
            override fun shouldWrap(consumer: Consumer<*>): Boolean =
                consumer::class.java.canonicalName.matches(Regex(packageName))
        }
    }

    class Not(
        private val wrapped: WrappingCondition
    ) :
        WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>): Boolean =
            !wrapped.shouldWrap(consumer)
    }

    class EitherOr(
        private val condition: () -> Boolean,
        private val wrapped1: WrappingCondition,
        private val wrapped2: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>) =
            if (condition()) wrapped1.shouldWrap(consumer) else wrapped2.shouldWrap(consumer)
    }

    class AnyOf(
        private vararg val wrapped: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>) =
            wrapped.any { it.shouldWrap(consumer) }
    }

    class AllOf(
        private vararg val wrapped: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>) =
            wrapped.all { it.shouldWrap(consumer) }
    }
}

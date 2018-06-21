package com.badoo.mvicore.consumer.middlewareconfig

import io.reactivex.functions.Consumer

interface WrappingCondition {

    fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) : Boolean

    object Always : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) =
            true
    }

    object Never : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) =
            false
    }

    class Conditional(
        private val condition: () -> Boolean
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) =
            condition()
    }

    object IsStandalone: WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
            standalone
    }

    object IsNamed: WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
            name != null
    }

    sealed class Name {
        class SimpleMatcher(private val pattern: String) :
            WrappingCondition {
            override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
                name != null && name.indexOf(pattern) != -1
        }

        class RegexMatcher(private val pattern: String) :
            WrappingCondition {
            override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
                name != null && Regex(pattern).containsMatchIn(name)
        }
    }

    class InstanceOf(
        private val clz: Class<*>
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
            clz.isInstance(consumer)
    }

    sealed class PackageName {
        class SimpleMatcher(private val packageName: String) :
            WrappingCondition {
            override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
                consumer::class.java.canonicalName.indexOf(packageName) != -1
        }

        class RegexMatcher(private val packageName: String) :
            WrappingCondition {
            override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
                Regex(packageName).containsMatchIn(consumer::class.java.canonicalName)
        }
    }

    class Not(
        private val wrapped: WrappingCondition
    ) :
        WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean): Boolean =
            !wrapped.shouldWrap(consumer, name, standalone)
    }

    class EitherOr(
        private val condition: () -> Boolean,
        private val wrapped1: WrappingCondition,
        private val wrapped2: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) =
            if (condition()) wrapped1.shouldWrap(consumer, name, standalone) else wrapped2.shouldWrap(
                consumer,
                name,
                standalone
            )
    }

    class AnyOf(
        private vararg val wrapped: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) =
            wrapped.any { it.shouldWrap(consumer, name, standalone) }
    }

    class AllOf(
        private vararg val wrapped: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(consumer: Consumer<*>, name: String?, standalone: Boolean) =
            wrapped.all { it.shouldWrap(consumer, name, standalone) }
    }
}

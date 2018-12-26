package com.badoo.mvicore.consumer.middlewareconfig

interface WrappingCondition {

    fun shouldWrap(target: Any, name: String?, standalone: Boolean) : Boolean

    object Always : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean) =
            true
    }

    object Never : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean) =
            false
    }

    class Conditional(
        private val condition: () -> Boolean
    ) : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean) =
            condition()
    }

    object IsStandalone: WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
            standalone
    }

    object IsNamed: WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
            name != null
    }

    sealed class Name {
        class SimpleMatcher(private val pattern: String) :
            WrappingCondition {
            override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
                name != null && name.indexOf(pattern) != -1
        }

        class RegexMatcher(private val pattern: String) :
            WrappingCondition {
            override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
                name != null && Regex(pattern).containsMatchIn(name)
        }
    }

    class InstanceOf(
        private val clz: Class<*>
    ) : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
            clz.isInstance(target)
    }

    sealed class PackageName {
        class SimpleMatcher(private val packageName: String) :
            WrappingCondition {
            override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
                target::class.java.canonicalName.indexOf(packageName) != -1
        }

        class RegexMatcher(private val packageName: String) :
            WrappingCondition {
            override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
                Regex(packageName).containsMatchIn(target::class.java.canonicalName)
        }
    }

    class Not(
        private val wrapped: WrappingCondition
    ) :
        WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean =
            !wrapped.shouldWrap(target, name, standalone)
    }

    class EitherOr(
        private val condition: () -> Boolean,
        private val wrapped1: WrappingCondition,
        private val wrapped2: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean) =
            if (condition()) wrapped1.shouldWrap(target, name, standalone) else wrapped2.shouldWrap(
                target,
                name,
                standalone
            )
    }

    class AnyOf(
        private vararg val wrapped: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean) =
            wrapped.any { it.shouldWrap(target, name, standalone) }
    }

    class AllOf(
        private vararg val wrapped: WrappingCondition
    ) : WrappingCondition {
        override fun shouldWrap(target: Any, name: String?, standalone: Boolean) =
            wrapped.all { it.shouldWrap(target, name, standalone) }
    }
}

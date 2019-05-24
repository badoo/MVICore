# Middleware configurations

## What is a Middleware configuration?

It is defined as a pair of a condition and a list of factories:

```kotlin
data class MiddlewareConfiguration(
    private val condition: WrappingCondition,
    private val factories: List<ConsumerMiddlewareFactory<*>>
)
```

If the `WrappingCondition` returns `true`, all the `Middlewares` created by `factories` will be wrapped around the target.

If it returns `false`, the original object is returned without any modifications.

## What is a WrappingCondition?

`WrappingCondition` is an interface with the sole responsibility of deciding whether any target should be wrapped or not:

```kotlin
interface WrappingCondition {

    fun shouldWrap(target: Any, name: String?, standalone: Boolean) : Boolean
}
```

The interface contains implementations for most common case checks, such as:

```kotlin
// Implementations checking the target      -- returns true if the target:
WrappingCondition.IsNamed                   // ...has a name
WrappingCondition.Name.SimpleMatcher        // ...has a name that contains a given substring
WrappingCondition.Name.Regex                // ...has a name that matches a given regex
WrappingCondition.InstanceOf                // ...is an instance of a given class / interface
WrappingCondition.PackageName.SimpleMatcher // ...has a package name that contains a given substring
WrappingCondition.PackageName.Regex         // ...has a package name that matches a given regex
WrappingCondition.IsStandalone              // ...is standalone (not part of a binding)

// Implementations checking a condition     -- returns true:
WrappingCondition.Always                    // ...always
WrappingCondition.Never                     // ...never
WrappingCondition.Conditional               // ...if a given () -> Boolean lambda returns true

// Implementations delegating to others     -- returns true if:
WrappingCondition.Not                       // the passed WrappingCondition returns false
WrappingCondition.EitherOr                  // delegates to one of two given WrappingConditions based on a condition
WrappingCondition.AnyOf                     // any of the passed WrappingConditions return true
WrappingCondition.AllOf                     // all of the passed WrappingConditions return true

```

If the above is not enough for any reason, you can write your own implementation of the interface.

## What is a ConsumerMiddlewareFactory?

Basically just a lambda to create a `Middleware` given a `Consumer<T>`:

```kotlin
typealias ConsumerMiddlewareFactory<T> = (Consumer<T>) -> ConsumerMiddleware<T>

// In practice:
val middlewareFactory: ConsumerMiddlewareFactory<*> = { consumer -> SomeMiddleware(consumer) }
```

## Ok, how do I use it?

Let's say:

1. You want to add `LoggingMiddleware` to all of your bindings and consumers

and

2. You only want to add `PlaybackMiddleware` to bindings and consumers if all of these hold true:
    - it's a debug build
    - they are named
    - they are in a certain package
    - but they are definitely not an instance of some class

Here's how you would do it:

```kotlin
Middlewares.configurations.add(
    MiddlewareConfiguration(
        condition = WrappingCondition.Always,
        factories = listOf(
            { consumer -> LoggingMiddleware(consumer, { Timber.d(it) }) }
        )
    )
)


Middlewares.configurations.add(
    MiddlewareConfiguration(
        condition = WrappingCondition.AllOf(
            WrappingCondition.Conditional { BuildConfig.DEBUG },
            WrappingCondition.IsNamed,
            WrappingCondition.PackageName.SimpleMatcher("com.example.package"),
            WrappingCondition.Not(
                WrappingCondition.InstanceOf(SomeClass::class.java)
            )
        ),
        factories = listOf(
            { consumer -> PlaybackMiddleware(consumer, recordStore, { Timber.d(it) }) }
        )
    )
)
```

## Under the hood

Whenever you call `.wrap` a `Consumer<T>` manually, or whenever the `Binder` does the same automatically, the extension function will apply your list of `MiddlewareConfiguration` objects:

```kotlin
fun <T : Any> Consumer<T>.wrap(
    name: String? = null,
    // remainder omitted
): Consumer<T> {
    // remainder omitted

    Middlewares.configurations.forEach {
        current = it.applyOn(current, target, name, standalone)
    }

    // remainder omitted

    return current
}
```

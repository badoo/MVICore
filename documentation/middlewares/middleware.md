# What is a middleware and why is it good for me?

## TL;DR

You can extract the cross-cutting concerns of your app (like logging) and put them into `Middlewares` that can be used as generic plugins instead of copy-pasting the same solution over and over and polluting your code.

## The big picture

You can picture this like a wrapped input:

0. No middleware: `Source -> Input`

1. Adding a layer of middleware: `Source -> Middleware(Input)`

2. Adding n layers of middleware: `Source -> Middleware(Middleware(...Middleware(Input)...))`

Every layer of `Middleware` forwards its input to its wrapped inner core (whether its the actual `Input`, or just another layer of `Middleware`).

Whether or not an `Input` is wrapped in `Middlewares` is unknown to the `Source`.

## The base idea

Consider the `Consumer<T>` interface:

```java
package io.reactivex.functions;

/**
 * A functional interface (callback) that accepts a single value.
 * @param <T> the value type
 */
public interface Consumer<T> {
    /**
     * Consume the given value.
     * @param t the value
     * @throws Exception on error
     */
    void accept(T t) throws Exception;
}

```

We can easily create a wrapper layer around it:

```kotlin
abstract class Middleware<T>(
    private val wrapped: Consumer<T>
) : Consumer<T> {

    override fun accept(t: T) {
        // do whatever you want before passing it to wrapped...
        wrapped.accept(t)
        // ... or after it
        // ... or even modify what's passed down to wrapped
    }
}
```

Let's play around with it and add some simple implementations:

```kotlin
class HelloMiddleware(
    private val wrapped: Consumer<String>
) : Middleware<String>(wrapped) {

    override fun accept(t: String) {
        wrapped.accept("Hello $t")
    }
}

class ReverserMiddleware(
    private val wrapped: Consumer<String>
) : Middleware<String>(wrapped) {

    override fun accept(t: String) {
        wrapped.accept(t.reversed())
    }
}

class LoggingMiddleware<T>(
    private val wrapped: Consumer<T>,
    private val logger: (String) -> Unit
) : Middleware<T>(wrapped) {

    override fun accept(t: T) {
        logger.invoke("LoggingMiddleware: element - $t")
        wrapped.accept(t)
    }
}
```

Now we can compose them and take them for a test ride:

```kotlin
val target: Consumer<String> = TODO()

val wrappedWithMiddlewares: Consumer<String> =
    ReverserMiddleware(
        HelloMiddleware(
            LoggingMiddleware(
                { System.out.println(it) },
                target
            )
        )
    )

wrappedWithMiddlewares.accept("MVICore")

// Should output to console:
// "LoggingMiddleware: element - Hello erocIVM!"
```

Explanation: the order in which an element is passing through the `Middleware` layers is from the outermost -> inwards. In the above example this means that the passed in string:

1. gets reversed
2. gets decorated with Hello %s!
3. gets logged

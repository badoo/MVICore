## LoggingMiddleware

## Logging - out of the box

MVICore comes with a configurable `LoggingMiddleware` that you can use right away.

```kotlin
val wrapped = LoggingMiddleware(consumer, { System.out.println(it) })
```

The actual logging part is not hardcoded, so you can set your preferred way of producing output.

The constructor also accepts an optional `Configuration` object if you want to modify the templates it uses. Check the [actual file](https://github.com/badoo/MVICore/blob/master/mvicore/src/main/java/com/badoo/mvicore/consumer/middleware/LoggingMiddleware.kt) for more details.

# Advanced binder usage

!!! note
    This section describes API that is optional and meant to help in specific cases.
   
## Changing reactive chain between input and output

As described [here](./binder/#binding-reactive-endpoints), we use a transformer to convert output to input.
```kotlin
val output: ObservableSource<A> = TODO()
val input: Consumer<B> = TODO()
val transformer: (A) -> B? = TODO()

binder.bind(output to input using transformer)
```

However, sometimes it is not enough to convert events one to one. 
For these cases, we can replace transformer with `Connector<A, B>`.
```kotlin
object InputToOutput : Connector<A, B> {
    override fun invoke(source: ObservableSource<A>): ObservableSource<B> =
        Observable.wrap(source)
            // TODO modify chain
            .map { a -> TODO() }
}
val output: ObservableSource<A> = TODO()
val input: Consumer<B> = TODO()

binder.bind(output to input using InputToOutput)
```

## Naming connections

And you can optionally give names to any connection:
```kotlin
binder.bind(input to output named "MyConnection")
// or
binder.bind(input to output using transformer named "MyConnection")
```

Naming a connection signals that it's important to you. This will make more sense when we'll add `Middlewares`:

- You'll see connections with their respective names in the time-travel debug menu
- You'll see connection names in logs if you use LoggingMiddleware
- You can opt to dynamically add `Middlewares` only to named connections (if that's what you want)

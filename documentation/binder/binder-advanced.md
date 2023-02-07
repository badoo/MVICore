# Advanced Binder usage

!!! note
    This section describes API that is optional and meant to help in specific cases.
   
## Changing reactive chain between input and output

As described [here](../binder/#binding-reactive-endpoints), 
we can use a simple transformer to convert between output and input types.
```kotlin
val output: ObservableSource<A> = TODO()
val input: Consumer<B> = TODO()
val transformer: (A) -> B? = TODO()

binder.bind(output to input using transformer)
```

However, sometimes this is not enough. 

For more complex cases, we can use a `Connector<A, B>` instead, which is also able to manipulate the stream.
```kotlin
object OutputToInput : Connector<A, B> {
    override fun invoke(source: ObservableSource<out A>): ObservableSource<B> =
        Observable.wrap(source)
            // TODO transform stream
            .map { a -> TODO() }
}
val output: ObservableSource<A> = TODO()
val input: Consumer<B> = TODO()

binder.bind(output to input using OutputToInput)
```

!!! note
    Try to keep your `Connector` simple.
    Common use-cases can be to add `.distinctUntilChanged()`, `.debounce()`, etc., but don't overcomplicate it!

## Naming connections

You can optionally give names to any connection:
```kotlin
binder.bind(input to output named "MyConnection")
// or
binder.bind(input to output using transformer named "MyConnection")
```

Naming a connection signals that it's important to you. This will make more sense when we'll add `Middlewares`:

- You'll see connections with their respective names in the time-travel debug menu
- You'll see connection names in logs if you use LoggingMiddleware
- You can opt to dynamically add `Middlewares` only to named connections (if that's what you want)

## Setting connections observation scheduler

You can optionally set the observation scheduler for any connection:
```kotlin
binder.bind(input to output observeOn scheduler)
```

You can also use `Binder.observeOn` to reduce repetition:
```kotlin
binder.observeOn(scheduler) {
    bind(input1 to output1)
    bind(input2 to output2)
}
```

Specifying an observation scheduler ensures that the output is called on the specified scheduler.

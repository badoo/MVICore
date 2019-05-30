# Binder usage and lifecycle

## What is the Binder and why is it good for me?

If you wrote your first `Feature`, now you may wonder how to start using it.

Do you subscribe to its state directly? Do you call `.accept(wish)` on it manually?

Well, you can, but there are better ways to do that, which also come with some bonuses.

Remember when in the [Core concepts](../features/coreconcepts.md) we said that
`Feature` is a `Consumer` of `Wish` and an `ObservableSource` of `State`? And that in general, the framework is working with outputs of type `ObservableSource<T>` and inputs of type `Consumer<T>`?

The `Binder` is a tool that can:

- automatically connect those outputs to those inputs by a subscription using a super simple syntax
- dispose of this subscription when its lifecycle expires
- automatically add `Middlewares` around all inputs (logging and time travel debugging, or your custom one)


## Binder creation

Creating an instance is as simple as:

```kotlin
val binder = Binder()
```

with manual disposal, or

```kotlin
val binder = Binder(lifecycle)
```

for automatic disposal of the created bindings when lifecycle expires (more on that below).

## Binding reactive endpoints

You can connect outputs and inputs directly if they are of the same type:
```kotlin
val output: ObservableSource<A> = TODO()
val input: Consumer<A> = TODO()

binder.bind(output to input)
```


Or using a transformer if they are of different types:
```kotlin
val output: ObservableSource<A> = TODO()
val input: Consumer<B> = TODO()
val transformer: (A) -> B = TODO()

binder.bind(output to input using transformer)
```

You can easily modify the reactive chain between them:
```kotlin
object DistinctInputToOutput : Connector<A, B> {
    override fun invoke(source: ObservableSource<A>): ObservableSource<B> =
        Observable.wrap(source)
            .distinctUntilChanged()
            .map { a -> TODO() }
}
val output: ObservableSource<A> = TODO()
val input: Consumer<B> = TODO()

binder.bind(output to input using DistinctInputToOutput)
```

And you can optionally give names to any of those connections:
```kotlin
binder.bind(input to output named "MyConnection")
// or
binder.bind(input to output using transformer named "MyConnection")
```

Naming a connection signals that it's important to you. This will make more sense later when we'll add `Middlewares`:

- You'll see connections with their respective names in the time-travel debug menu
- You'll see connection names in logs if you use LoggingMiddleware
- You can opt to dynamically add `Middlewares` only to named connections (if that's what you want)


## Lifecycle handling

Since all connections created by the `Binder` are rx subscriptions under the hood, disposing needs to be taken care of.

At the simplest:

```kotlin
val binder = Binder()

// bind stuff
binder.bind(a to b)
binder.bind(c to d)

// don't forget to call later
binder.dispose()
```

But you don't need to do this manually. `Binder` can take an instance of `Lifecycle` in its constructor, which is really only a way to signal termination:

```kotlin
interface Lifecycle : ObservableSource<Lifecycle.Event> {

    enum class Event {
        BEGIN,  // currently not used
        END     // signals termination
    }
}
```

A `Lifecycle` instance can be created by mapping any other observable stream:
```kotlin
val stream: Observable<T> = TODO()
val lifecycle = Lifecycle.wrap(stream.map { Lifecycle.Event.END })
```

Or if you are on Android and using the `mvicore-android` dependency, you can leverage the `AndroidBinderLifecycle` class to automatically create Binder lifecycle from Android lifecycle:
```kotlin
val lifecycle = AndroidBinderLifecycle(activity) // or any other Android LifecycleOwner
```

In both the above cases you don't need to worry about disposing: whenever `Lifecycle` signals it, the `Binder` instance will dispose of the created subscriptions:

```kotlin
val binder = Binder(lifecycle)

// bind stuff
binder.bind(a to b)
binder.bind(c to d)

// no need to dispose manually, will be handled automatically
```

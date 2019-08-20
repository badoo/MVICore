# Core concepts

## Sources and consumers

MVICore works with sources and consumers of observable data at its heart.

That is:

- outputs are of type `:::kotlin ObservableSource<T>`
- inputs are of type `:::kotlin Consumer<T>`


## Stores

The `:::kotlin Store<Wish, State>` interface defines the outline of a state machine that
- can receive objects of type `Wish` on its input
- can produce objects of type `State` on its output
- in addition, it holds on to the latest `State`, which can be queried without subscribing to the Store

```kotlin
interface Store<Wish : Any, State : Any> : Consumer<Wish>, ObservableSource<State> {

    val state: State
}
```

`State` is meant to be immutable, for which we'll use Kotlin data classes.

## Features

A `Feature` is a `Store` with the addition of:

- being `Disposable`, as it might hold subscriptions
- being a source of `News`

```kotlin
interface Feature<Wish : Any, State : Any, News: Any> : Store<Wish, State>, Disposable {

    val news: ObservableSource<News>
}

```

`News` marks the type of the events a `Feature` can emit. These are pieces of information you don’t want to store in the state, just fire off once when they happen.

More about that in the chapter [Events that should be consumed only once](news.md).

## Features are hot observables

`Features` are not cold observables: they do not wait for subscriptions to start working, and they are not scoped by subscriptions to them. They are push-based (pushing out new states automatically) and not pull-based (producing something upon a subscription). 

This is a designed feature of the library.

`Features` are supposed to be able to be active in the background, and have possibly many, differently scoped inputs / outputs to them. 

Two things follow from this:

1. A `Feature` starts working immediately on creation, and emits its initial state.
2. A `Feature` always needs to be disposed of when its lifecycle should end. That means it's your responsibility to call `.dispose()` on it, so that any asynchronous jobs still executing are properly disposed of.

!!! bug "Bottom line"
    Don't forget to call `.dispose()` at the end of the `Feature`'s lifecycle

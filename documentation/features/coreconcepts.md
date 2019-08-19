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

A `Feature` is really only a `Store` with the addition of:

- being `Disposable`, as it might hold subscriptions
- being a source of `News`

```kotlin
interface Feature<Wish : Any, State : Any, News: Any> : Store<Wish, State>, Disposable {

    val news: ObservableSource<News>
}

```

`News` marks the type of the events a `Feature` can emit. These are pieces of information you don’t want to store in the state, just fire off once when they happen.

More about that in the chapter [Events that should be consumed only once](news.md).

## Feature lifecycle

Most probably you want your `Feature` to live longer than the current screen. This has many benefits:

- You don't need to override `onSaveInstanceState` and `onRestoreInstanceState` in your Android `Activity`. The `Feature` holds on to the state, the `Activity` connects to it in its `onCreate` method, and because the stream of `State`s inside is a `BehaviorSubject`, it will replay the latest one to your `Activity` upon subscription.
- Rotating the screen? Not an issue, your state is automatically there.
- Your `Feature` is not on screen because the user navigated away to some other screen? Not a problem. It's still alive and listening in the background to update its state if needed, so when the user comes back to the screen where it's visible, it immediately shows the most up-to-date state.

This probably implies that your `Feature` is living in some kind of a DI scope, and it's your responsibility to call `.dispose()` on it once this scope goes away, so that any asynchronous jobs still executing are properly disposed of.

!!! bug "Bottom line"
    Don't forget to call `.dispose()` at the end of the `Feature`'s lifecycle

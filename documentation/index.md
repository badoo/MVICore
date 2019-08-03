# MVICore

## What's this?

MVICore is a modern, Kotlin-based MVI framework:
- **Scaling with complexity**: operate with a single Reducer if needed, with the option of having the full power of additional components to handle more complex cases
- **Event handling**: A solution to handling events that you don’t want to store in the state
- **Reactive component binding**: A super simple API to bind your reactive endpoints to each other with automatic lifecycle handling
- **Custom Middlewares**: for every single component in the system, with flexible configuration options
- **Logger**: An out-of-the-box logger Middleware
- **Time Travel Debugger**: for ALL of your reactive components (not just your state machine!) with UI controls for recording and playback

## The big picture

### 1. Create your state machine
```kotlin
class SimpleFeature : ReducerFeature<Wish, State, Nothing>(
    initialState = State(),
    reducer = ReducerImpl()
) {
    // Define your immutable state as a Kotlin data class
    data class State(
        val counter: Int = 0
    )

    // Define the ways it could be affected
    sealed class Wish {
        object IncreaseCounter : Wish()
        data class MultiplyBy(val value: Int) : Wish()
    }

    // Define your reducer
    class ReducerImpl : Reducer<State, Wish> {
        override fun invoke(state: State, wish: Wish): State =

            // Leverage the power of exhaustive when over Kotlin sealed classes
            when (wish) {

                // Create the next state based on the current one
                IncreaseCounter -> state.copy(
                    counter = state.counter + 1
                )

                // Create the next state based on the current one
                is MultiplyBy -> state.copy(
                    counter = state.counter * wish.value
                )
            }
    }
}

```

!!! note
    ```Feature``` has additional components to solve problems like side-effects, events, initialisation, internal jobs in a standardised way. For a full list check [Features](features/coreconcepts.md) section to see what's possible.

### 2. Your state machine is reactive

```kotlin
val feature = SimpleFeature()

// Now you can observe and subscribe to its state changes:
Observable.wrap(feature).subscribe { state -> TODO() }

// And it's also a Consumer of Wishes. Trigger some state changes:
Observable.just(Wish.IncreaseCounter).subscribe(feature)
```

Actually, **don't use it the above way**! We can do so much better:

### 3. Use the Binder

For connecting your reactive components.
Comes with automatic lifecycle handling and invoking transformations:

```kotlin
val binder = Binder(lifecycle)
binder.bind(view to feature using ViewEventToWish)
binder.bind(feature to view using StateToViewModel)
```

## Download

Available through jitpack.

Add the maven repo to your root `build.gradle`

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependencies:

- Framework:
```groovy
implementation 'com.github.badoo.mvicore:mvicore:{latest-version}'
```

- Helper classes for Android:
```groovy
implementation 'com.github.badoo.mvicore:mvicore-android:{latest-version}'
```

- ModelWatcher for efficient view updates
```groovy
implementation 'com.github.badoo.mvicore:mvicore-diff:{latest-version}'
```

- Time Travel Debugger controls in a [DebugDrawer](https://github.com/palaima/DebugDrawer) module (You need to add the dependencies to DebugDrawer and configure it yourself before you can use this):
```groovy
implementation 'com.github.badoo.mvicore:mvicore-debugdrawer:{latest-version}'
```

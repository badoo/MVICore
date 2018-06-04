# Handling async jobs

Previous: [2. Your first and simplest feature](reducerfeature.md)

Next: [4. Going full-featured](fullfeatured.md)

[Go up one level](README.md)

## Actor

If you have
- anything asynchronous
- more complex requirements how some `Wish` will modify the `State`

then now we need to distinguish between
- incoming `Wish`
- an actual `Effect` that is applied over the `State` using the `Reducer`

And now we need a mapping between the two. The `Actor` is basically a function doing just that:

```kotlin
typealias Actor<State, Wish, Effect> = (State, Wish) -> Observable<Effect>
```

This means that now we can consider an incoming `Wish` and our current `State`, and based on them we can do some operations that will emit `Effect`s to change our `State`.

> _Note: the operations do not have to be asynchronous. You can still use `Observable.just()` to return one or more `Effect`s immediately. The added power here is that you can do that conditionally based on the current `State`_
>
> _E.g. your `Feature` represents a form, and then based on the result of form validation over the current state, you can emit different `Effect`s to signal validation success or error._

## Important ##

Since invocations of the reducer must always happen on the same thread, you must ensure that you observe results of your asynchronous jobs on that thread. In Android, this practically means calling `.observeOn(AndroidSchedulers.mainThread())`

## Excercise #2

### Task
- Let's talk to an async service to load some data
- Let's signal whether we are in progress of loading, successfully loaded, or if an error has happened

### Solution using ActorReducerFeature

```kotlin
class Feature2 : ActorReducerFeature<Wish, Effect, State>(
    initialState = State(),
    actor = ActorImpl(),
    reducer = ReducerImpl()
) {

    data class State(
        val isLoading: Boolean = false,
        val payload: String? = null
    )

    sealed class Wish {
        object LoadNewData : Wish()
    }

    sealed class Effect {
        object StartedLoading : Effect()
        data class FinishedWithSuccess(val payload : String) : Effect()
        data class FinishedWithError(val throwable: Throwable) : Effect(), News
    }

    class ActorImpl : Actor<State, Wish, Effect> {
        private val service: Observable<String> = TODO()

        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            if (!state.isLoading) {
                LoadNewData -> {
                    service
                        .observeOn(AndroidSchedulers.mainThread())
                        .map { FinishedWithSuccess(payload = it) as Effect }
                        .startWith(StartedLoading)
                        .onErrorReturn { FinishedWithError(it) }
                }
            } else {
                Observable.empty()
            }
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = when (effect) {
            is StartedLoading -> state.copy(
                isLoading = true
            )
            is FinishedWithSuccess -> state.copy(
                isLoading = false,
                payload = effect.payload
            )
            is FinishedWithError -> state.copy(
                isLoading = false
            )
        }
    }
}
```

Under the hood, `ActorReducerFeature` is a subclass of `BaseFeature` giving you a subset of all the possibilities there.

It will also wire everything up for you (reacting to a `Wish`, calling your `Actor` and subscribing to the `Observable<Effect>` returned by it, and calling your `Reducer` to emit your next `State`).

> _Note: in this example, the error result is not stored in the state, but rather, the `FinishedWithError` effect is marked as being `News`._
>
>_This means, that it will automatically emit a one-time signal through `Feature.news` onto which you can subscribe to handle the error (log it, show a toast, etc.)_
>
>_But if you wish, you can still add a field in the `State` to store the error, just don't forget to reset it in the `Reducer` upon the next `StartedLoading` or `FinishedWithSuccess` effects._
>
>_Another approach would be to use a Kotlin sealed class, or the functional `Either<A, B>` type for the payload, where `A` would be the error, `B` would be actual data. Really only up to you._

### When should you use ActorReducerFeature
- There are async jobs in your Feature
- There's some extra business logic involving how to react to a `Wish` conditionally

---

Next: [4. Going full-featured](fullfeatured.md)

[Go up one level](README.md)

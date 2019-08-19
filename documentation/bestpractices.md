## FAQ & Best practices

### I. Feature disposal

Never forget to call `feature.dispose()` when the context your `Feature` is living in goes away!

### II. Keep your Reducers dumb

The idea is that `Reducer` should contain only resolution to how an `Effect` modifies the `State` directly.

```kotlin
class ReducerImpl : Reducer<State, Effect> {
    override fun invoke(state: State, effect: Effect): State = when (effect) {
        // This is fine:
        is Effect1 -> state.copy(someFlag = true)
        is Effect2 -> state.copy(someData = effect.data)
        is Effect3 -> state.copy(counter = state.counter + 1)

        // Don't do this:
        is Effect4 -> if (someCondition) (someFlag = true) else state.copy(counter = state.counter + 1)
    }
}
```

If you find yourself adding conditionals, it's a smell that probably business logic is creeping from your `Actor` to your `Reducer`.

Resolution: `Actor` is the intended place for business logic:

1. Create `Effects` with meaningful names to describe what can happen to your `State`
2. Decide what happens inside your `Actor`, based on any conditional logic or async execution, and emit the corresponding `Effects`
3. Use your `Reducer` only to implement how it modifies the `State`

### III. Effects only in Actor

Once the complexity grows inside your `Actor`, you might be tempted to extract some responsibilities to other classes.

This is fine, however, don't do this:

```kotlin
class ActorImpl(
    private val executor1: Executor1,
    private val executor2: Executor2
) : Actor<State, Wish, Effect> {

    override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
        is Wish1 -> executor1.doSomething()
        is Wish2 -> executor2.doSomething()
    }
}

// In some other files:

class Executor1 {
    fun doSomething(): Observable<Effect> = TODO()
}

class Executor2 {
    fun doSomething(): Observable<Effect> = TODO()
}
```

This way the reader of your code can have no simple understanding of what `Effect` is the result of what and where.

Resolution:

1. Keep your `Effects` internal to your `Feature`
2. In your extracted classes use only local result types
3. Map those results to `Effects` in your `Actor`, ensuring that all business logic is understandable in a high-level overview in one place.

Example:

```kotlin
class ActorImpl(
    private val executor1: Executor1,
    private val executor2: Executor2
) : Actor<State, Wish, Effect> {

    override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
        is Wish1 -> executor1
            .doSomething()
            .map { when (it) {
                is SomethingHappened1 -> Effect1(it.data)
                is SomethingHappened2 -> Effect2
            }}

        is Wish2 -> executor2
            .doSomething()
            .map { when (it) {
                is SomethingElseHappened1 -> Effect3
                is SomethingElseHappened2 -> Effect4(it.error)
            }}
    }
}

// In some other files:

class Executor1 {
    fun doSomething(): Observable<LocalResult> = TODO()

    sealed class LocalResult {
        data class SomethingHappened1(val data: Any) : LocalResult()
        object SomethingHappened2 : LocalResult()
    }
}

class Executor2 {
    fun doSomething(): Observable<LocalResult> = TODO()

    sealed class LocalResult {
        object SomethingElseHappened1 : LocalResult()
        data class SomethingElseHappened2(val error: Throwable) : LocalResult()
    }
}
```

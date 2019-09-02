# Keep your Reducers dumb

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

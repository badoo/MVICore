# Your first and simplest feature

Previous: [1. Core concepts](coreconcepts.md)

Next: [3. Handling async jobs](actorreducerfeature.md)

[Go up one level](README.md)


## Reducer

Your simplest `Feature` would be one which only uses a `Reducer` to create and emit new states based on the latest one and an incoming effect.

The Reducer is basically a function defining just that:

```kotlin
typealias Reducer<State, Effect> = (State, Effect) -> State
```

The name `Effect` is used here in the more generic context.

> _Note: Later there will be a distinction between `Wish`es and `Effect`s, but for now, they are one and the same in this simplest example._

## Important ##

Invocations of the reducer must always happen on the same thread to ensure that new `Effect`s are always applied to the latest `State` and we are not losing modifications.

> _If two threads were to read State<sup>n</sup>, then apply some `Effect` over it, one would derive State<sup>n+1'</sup>, while the other would derive  State<sup>n+1''</sup> from it, and depending on the order of execution, one or the other would be lost. By enforcing the single-thread policy, all `Effect`s are always applied to the latest state._

## Excercise #1
### Task
- Let's store a counter in our state
- Let's make it possible to increment this counter by a `Wish`

### Solution using ReducerFeature

Meet the simplest `Feature`, the `ReducerFeature`:

```kotlin
class Feature1 : ReducerFeature<Wish, State, Nothing>(
    initialState = State(),
    reducer = ReducerImpl()
) {

    data class State(
        val counter: Int = 0
    )

    sealed class Wish {
        object IncrementCounter : Wish()
    }

    class ReducerImpl : Reducer<State, Wish> {
        override fun invoke(state: State, wish: Wish): State = when (wish) {
            IncrementCounter -> state.copy(
                counter = state.counter + 1
            )
        }
    }
}
```



Under the hood, `ReducerFeature` is a subclass of `BaseFeature` giving you a subset of all the possibilities there.

It will also wire everything up for you (reacting to a `Wish`, calling your `Reducer`, emitting your next `State`).

### When should you use ReducerFeature
- There are no async jobs in your Feature
- There's no extra business logic. Whatever comes in as a `Wish`, always modifies the `State` without a question, and we just want to keep track of it.

---

Next: [3. Handling async jobs](actorreducerfeature.md)

[Go up one level](README.md)

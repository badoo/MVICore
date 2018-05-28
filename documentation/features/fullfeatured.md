# Going full-featured

Previous: [3. Handling async jobs](actorreducerfeature.md)

Next: [5. Bootstrappers](bootstrappers.md)

[Go up one level](README.md)

## DefaultFeature

If the reduced functionality of [ReducerFeature](reducerfeature.md) and [ActorReducerFeature](actorreducerfeature.md) is not enough for your case, this base class is your go-to.

DefaultFeature takes four generic parameters:

```DefaultFeature<Wish, Action, Effect, State>```

The new one here compared to the simpler Features is the `Action`.

## Actions

Use-case:
- you need some kind of an "internal `Wish`" to execute, but you don't want to leak it through your `Wish` sealed class, as it would make it publicly callable.
- you want to be able to trigger these "internal `Wish`es"

`Action` is a superset of `Wish` in the form of:

```kotlin
sealed class Wish {
    object PublicWish1 : Wish()
    object PublicWish2 : Wish()
    object PublicWish3 : Wish()
}

sealed class Action {
    data class Execute(val wish: Wish) : Action()
    object InvalidateCache : Action()
    object ReloadSomething : Action()
}
```

This has two implications:
1. For `DefaultFeature` to know how your public `Wish` maps to an `Action`, you need to supply a mapping function in the constructor
```kotlin
typealias WishToAction<Wish, Action> = (Wish) -> Action
```

2. Now your `Actor` will be acting upon `Action` instead of `Wish`

So any incoming `Wish` is mapped to an `Action`, and executed in the `Actor` along with all other `Action`s:

```kotlin
class MyComplexFeature : DefaultFeature<Wish, Action, Effect, State>(
    // ...remainder omitted...
    wishToAction = { Execute(it) },
    actor = ActorImpl(),
) {
    // ...remainder omitted...

    sealed class Wish {
        object PublicWish1 : Wish()
        object PublicWish2 : Wish()
        object PublicWish3 : Wish()
    }

    sealed class Action {
        data class Execute(val wish: Wish) : Action()
        object InvalidateCache : Action()
        object ReloadSomething : Action()
    }

    class ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> = when (action) {
            is Execute -> when (action.wish) {
                PublicWish1 -> TODO()
                PublicWish2 -> TODO()
                PublicWish3 -> TODO()
            }
            InvalidateCache -> TODO()
            ReloadSomething -> TODO()
        }
    }

    // ...remainder omitted...
}
```

So now you can have internal `Action`s, but how will you trigger them? Meet the `PostProcessor`.


## PostProcessor

The `PostProcessor` (as the name implies) will have a chance to react after a certain `Action` was mapped to a certain `Effect` which was used to create a new `State`. At this point, it can signal the need for additional `Action`s:

```kotlin
typealias PostProcessor<Action, Effect, State> = (Action, Effect, State) -> Action?
```

Using the example above this could be:

```kotlin
class MyComplexFeature : DefaultFeature<Wish, Action, Effect, State>(
    // ...remainder omitted...
    postProcessor = PostProcessorImpl()
) {
   // ...remainder omitted...

   class PostProcessorImpl : PostProcessor<Action, Effect, State> {
           // do anything based on action (contains wish), effect, state
           override fun invoke(action: Action, effect: Effect, state: State): Action? {
               if (state.i == 101) {
                   return InvalidateCache
               }

               return null
           }
       }
}
```

The implementation of `DefaultFeature` wires everything up for you from mapping your `Wish` to `Action`, calling your `Actor`, `Reducer`, and `PostProcessor` and emitting the next `State`.

---

Next: [5. Bootstrappers](bootstrappers.md)

[Go up one level](README.md)

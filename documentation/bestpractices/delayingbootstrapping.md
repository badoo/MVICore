# Delaying bootstrapping 

## Bootstrapping is immediate by default

As noted in the [core concepts](../../features/coreconcepts/) section, `Features` are essentially hot observables. It's worth to repeat this here, because there's an important aspect of it here: `Bootstrapper` is invoked immediately on `Feature` creation and by default will not wait for anything else.

You can picture the `Feature` as if it was a glorified `BehaviourSubject`: when you create that with `createDefault`, it emits its first state straight away and continues to emit whatever is put into it after creation.


## Missing initial emissions?

Following from this, it is possible that by the time you subscribe to your `Feature`, the actions triggered by `Bootstrapper` have already resulted in multiple state updates, and that you can't catch all of them.

If you are using your `Feature` as part of an MVI approach, then the last (current) state of it should always be enough to render the `View`, so in these cases it shouldn't be an issue at all.

But, as you can use a `Feature` as a more generic tool, in some rare cases it can be valid that you absolutely need to catch and evaluate all state/news emissions.


## Catching all emissions 

In a cross-cutting concern case (e.g. logging) you probably want to go with [middlewares](../../middlewares/middleware/). 

If that's not the case, you might want to delay bootstrapping for whatever reason outside of `Feature`. 

### Don't
What you don't want to do is create "Init" `Wish` for example. That results in it being a repeatable and public action on the `Feature` (nothing is stopping you from pushing the same `Wish` even multiple times from any other part of the code), and is considered a smell.

### Do
Rather, you can achieve the delay by injecting an `Observable` through the constructor of the `Feature` to the constructor of your `Bootstrapper` and use that stream to delay subscription:

```kotlin
// Feature
class SomeFeature(
    startSignal: Observable<Unit>
    // ...
) : BaseFeature<Wish, Action, Effect, State, News>(
    // ...
    bootstrapper = BootStrapperImpl(startSignal)
    // ...
) {
    // ...

    class BootStrapperImpl(
        private val startSignal: Observable<Unit>
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> = 
            just(Action.BootstrappingAction)
                .delaySubscription(startSignal)
                
    // ...
}


// Client code
val startSignal = PublishRelay.create<Unit>()
val feature = SomeFeature(startSignal)
// TODO do stuff, subscribe to feature, etc., and when you are ready:
startSignal.accept(Unit)
```

This way it's both non-repeatable and it's also not exposed to the public API (`Wishes`)


# Bootstrappers

Previous: [4. Going full-featured](fullfeatured.md)

Next: [6. News and inter-feature communication](news.md)

[Go up one level](README.md)

## Listening to remote sources

So far all of the triggers to our `Feature`s were internal:
- `Wish`es come from client code
- `Action`s are triggered inside `Feature` in the `PostProcessor`

But what if you need to react to let's say when the server sends you a push? You can of course listen to them outside of your `Feature` and then push `Wish`es, but this has its own disadvantages:
- Part of your business logic is now living outside of your class. If you are passing your component around for others to use, everyone will need to pay attention how to set it up. This is not correct.
- From outside you can only trigger public `Wish`es, not `Action`s

The `Bootstrapper` is solving exactly these problems.

## Bootstrapper

It's defined as:

```kotlin
typealias Bootstrapper<Action> = () -> Observable<Action>
```

which practically means, that when invoked, it can give you a stream of `Action`s. You can use this interface to add initial wiring to your `Feature`:

```kotlin
class BootstrapperImpl : Bootstrapper<Action> {
    private val service1: Observable<Any> = TODO()
    private val service2: Observable<Any> = TODO()

    override fun invoke(): Observable<Action> = Observable.merge<Action>(
        service1.map { InvalidateCache },
        service2.map { ReloadSomething }
    ).observeOn(AndroidSchedulers.mainThread())
}
```

Pass your `Bootstrapper` implementation in the constructor to either `ReducerFeature`, `ActorReducerFeature`, or `BaseFeature`:

```kotlin
class MyComplexFeature : BaseFeature<Wish, Action, Effect, State, News>(
    // ...remainder omitted...
    bootstrapper = BootstrapperImpl()
)
```

The implementation of `BaseFeature` wires it up for you.

---

Next: [6. News and inter-feature communication](news.md)

[Go up one level](README.md)

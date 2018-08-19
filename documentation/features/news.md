# News and inter-feature communication

Previous: [5. Bootstrappers](bootstrappers.md)

[Go up one level](README.md)

## News

As we’ve seen, the `Feature` interface has a third generic type of `News`:

```kotlin
interface Feature<Wish : Any, State : Any, News: Any> : Store<Wish, State>, Disposable {

    val news: ObservableSource<News>
}

```

News is the type that a Feature can use for signaling single events. This is to address a recurring issue with MVI approaches: how do you handle events that should be consumed only once? For example, a toast / snackbar message, or a redirect event.

If you store this information in the state, then it will remain there until you clear it - and until that is done, all subsequent subscribers to that state will receive it again. Sometimes that’s just what you want, but it’s also a valid case, that showing those messages later again makes no sense (if they are outdated and irrelevant now), or that going back to a screen which still holds a redirect order in its state will cause you problems.

The approach in this library is to create a separation between states and events, and not store the latter in the previous in any form. Rather, all Feature implementations can have their very own `NewsPublisher`:

```kotlin
typealias NewsPublisher<Action, Effect, State, News> = (Action, Effect, State) -> News?
```

Its invocation has access to all the parameters of the current execution chain (which action triggered which effect, which resulted in what new state), and by implementing it you can fabricate any condition based on those to signal emitting `News`.

You can pass a `NewsPublisher` implementation in your constructor to the base classes, and the framework will make sure to call it after each new state emission. Any News produced by it will then be observable through the `news` property of the feature automatically:

```kotlin
class FeatureImpl : ActorReducerFeature<Wish, Effect, State, News>(
    // ...remainder omitted...
    newsPublisher = NewsPublisherImpl()
) {

    // ...remainder omitted...

    sealed class Effect {
        // ...remainder omitted...
        data class ErrorLoading(val throwable : Throwable) : Effect()
    }

    sealed class News {
        data class ErrorExecutingRequest(val throwable: Throwable) : News()
    }

    class NewsPublisherImpl : NewsPublisher<Wish, Effect, State, News> {
        override fun invoke(wish: Wish, effect: Effect, state: State): News? = when (effect) {
            is ErrorLoading -> News.ErrorExecutingRequest(effect.throwable)
            else -> null
        }
    }
}
```

Then somewhere else with an instance of this class you can observe the `News` and react to them:
```kotlin
val featureImpl: FeatureImpl = TODO() // inject or create
val news: ObservableSource<FeatureImpl.News> = featureImpl.news

// subscribe to news or bind it to a Consumer<FeatureImpl.News>:

fun accept(news: FeatureImpl.News) {
    when (featureImpl.news) {
        is News.ErrorExecutingRequest -> doSomethingWith(news.throwable)
    }
}

fun doSomethingWith(throwable: Throwable) {
    // log it
    // show toast
    // etc.
}

```

So how is this beneficial to you?

To sum it up, using this approach allows you to:
- React on single events which would be probably inconvenient to store in the State
- React to _what_ happened rather than how it changed the state
- Inter-feature communication by mapping an important event from one feature to a wish of another (you could inject an instance of `Feature1` to `Feature2`, which could use `Feature1.news` as a source in its `Bootstrapper` to automatically react)

---

Previous: [5. Bootstrappers](bootstrappers.md)

[Go up one level](README.md)

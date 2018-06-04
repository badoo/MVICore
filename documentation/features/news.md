# News and inter-feature communication

Previous: [5. Bootstrappers](bootstrappers.md)

[Go up one level](README.md)

## News

Any `Effect` you have can be marked with the `News` marker interface:

```kotlin
sealed class Effect {
    object StartedLoading : Effect()
    data class FinishedWithSuccess(val payload : String) : Effect()
    data class FinishedWithError(val throwable: Throwable) : Effect(), News
}

```

The implementation in `BaseFeature` will check for `Effect`s whether they are an instance of `News` and push them down the `Feature`'s `news` property.

This allows you to:
- React on single events which would be probably inconvenient to store in the `State` (you need to clear it later when it is consumed, otherwise every state emission would trigger events again)
- Take this stream of `News`, map it to an other `Feature`'s `Wish` and pass it to that `Feature` to implement inter-feature communication. Do this wiring in the setup of your dependant `Feature`, probably in your DI configuration.

By using `News` you can react to _what_ happened rather than how it changed the state.

---

Previous: [5. Bootstrappers](bootstrappers.md)

[Go up one level](README.md)

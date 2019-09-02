# Saving / restoring Feature state

## Timecapsule

MVICore offers a `Timecapsule` interface you can use to save/restore the state of your `Feature`:

```kotlin
class SomeFeature(
    // pass an instance in constructor:
    timeCapsule: TimeCapsule<Parcelable>? = null
    // ...
) : ActorReducerFeature<Wish, Effect, State, News>(
    // initial state depends on having something inside TimeCapsule,
    // or falling back to default value:
    initialState = timeCapsule?.get(SomeFeature::class.java) ?: State()
    // ...
) {
    init {
        // Register with the same key:
        timeCapsule?.register(SomeFeature::class.java) { state }
    }

    @Parcelize
    data class State(
        val someField: Int = 0
    ) : Parcelable
    
    // ...
}    
```

It's the responsibility of the actual `TimeCapsule` implementation to actually call the supplied lambda and grab the `state` when it needs to persist itself.

## AndroidTimeCapsule

The `mvicore-android` module adds an implementation of the interface, `AndroidTimeCapsule`. You can create it with a `Bundle` and you call `saveState(outState: Bundle)` on it:

```kotlin
val savedInstanceState: Bundle = TODO() // get it from Android
val timeCapsule = AndroidTimeCapsule(savedInstanceState)
val feature = SomeFeature(timeCapsule) // restore

// later:
fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    timeCapsule.saveState(outState) // save
}
```

`AndroidTimeCapsule` will make sure to persist the state of all `Features` registered to it.

!!! note ""
    You are free to use the same instance of `AndroidTimeCapsule` to persist multiple `Features`, provided the keys they register with are unique (pay attention to this when persisting multiple instances of the same class!).

## Resetting some information

Consider this case:

  1. your `Feature` can do long running operations
  2. your `State` has an `isLoading` flag to reflect when the operation is running
  3. you show a loading spinner on the UI depending on the value of this flag
  4. after saving state to `Bundle`, your `Feature` is disposed, cancelling the operation
  5. after restoring state from `Bundle`, `isLoading` is restored with a value of `true`, rendering a loading spinner on the UI, even though there's no actual operation backing this visual information 
    
In such cases you can reflect cancelled operations with the state accessor lambda when registering in `TimeCapsule`:

```kotlin
class SomeFeature(
    timeCapsule: TimeCapsule<Parcelable>? = null
    // ... 
) : ActorReducerFeature<Wish, Effect, State, News>(
    // ...
    initialState = timeCapsule?.get(SomeFeature::class.java) ?: State()
    // ...
) {
    init {
        // Reset some fields to reflect cancelled operations:
        timeCapsule?.register(SomeFeature::class.java) { state.copy(
            isLoading = false
        )}
    }

    @Parcelize
    data class State(
        val someField: Int = 0,
        val isLoading: Boolean = false
    ) : Parcelable
    
    // ...
}    
```

In the above example `someField` will be saved/restored with its actual value, while `isLoading` is always saved with value of `false`.

Upon restoring the `Feature`, you can implement logic in `Bootstrapper` to decide if you need to restart a cancelled operation. Then, only when it's actually loading, your `isLoading` flag will be set to `true` again. This way, your UI always reflects the actual state of loading.

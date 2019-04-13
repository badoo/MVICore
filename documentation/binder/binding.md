# Binding Features to the UI (and other reactive components)

## I have my Feature, now what?

Let's take a step by step approach how to connect our Features to the UI.

Let's suppose we have:

- `Feature1<Wish, State>`
- `Binder` instance
- A View, where we want to render the state of `Feature1`, and trigger some `Wish`es on them.

## Step 1: Direct binding

```kotlin
class View : Consumer<Feature1.State> {

    val binder: Binder = TODO()
    val feature: Feature1 = TODO()

    val button: Button = TODO()
    val counter: TextView = TODO()
    val image: ImageView = TODO()
    val progress: ProgressBar = TODO()

    fun onCreate() {
        setupBindings()
        setupViews()
    }

    fun setupBindings() {
        binder.bind(feature to this)
    }

    private fun setupViews() {
        button.setOnClickListener {
            // directly talking to feature
            feature.accept(Feature1.Wish.Foo)
        }
    }

    override fun accept(state: Feature1.State) {
        counter.text = (state.counter1 + state.counter2) % 2 // "complex logic"
        image.url = state.imageUrls.first { it.contains("imgur") } // "complex logic"
        if (state.isLoading) progress.visible() else progress.hide()
    }
}
```

In this example, `View` accepts `State` directly, and talks to `Feature1` directly. This is wrong for multiple reasons:

- The `View` is now tightly coupled to `Feature1`
- The `View` really shouldn't care where it gets the data it wants to display on the screen from. It shouldn't render data models, but rather view models that doesn't require local logic to transform (see the comments about "complex logic").
- The `View` has the additional responsibility of managing bindings

Let's fix these one by one.

## Step 2: Extract bindings

```kotlin
class Bindings @Inject constructor(
    private val feature: Feature1
) {
    val binder: Binder = TODO()

    fun setup(view: View) {
        binder.bind(feature to view)
    }
}
```

Now the extra concern is lifted from the `View`, and it only cares about its input (`State`) and output (triggering `Wish`). But let's not stop here.

## Step 3: Don't render the State, render a ViewModel

Define your `ViewModel` however you see fit. It should contain processed, "dumb", simple to display data only, and only what is actually required for your `View`:
```kotlin
data class ViewModel(
    val counter: Int,
    val imageUrl: String,
    val isLoading: Boolean
)
```

Convert the `State` to a `ViewModel` with a `ViewModelTransformer`:

```kotlin
object ViewModelTransformer : (Feature1.State) -> ViewModel {

    override fun invoke(state: Feature1.State): ViewModel =
        ViewModel(
            // 1. If the State stores data in another / more complex format,
            //      mapping to simple values should be done here, and not in the View
            // 2. Also the State might contain a lot more stuff,
            //      here we only pass on those actually needed for the View
            counter = (state.counter1 + state.counter2) % 2,
            imageUrl = state.imageUrls.first { it.contains("imgur") },
            isLoading = state.isLoading
        )
}

```

Modify `View` to consume `ViewModel`, it becomes much simpler without data model parsing logic:

```kotlin
class View : Consumer<ViewModel> {

    // remainder omitted

    override fun accept(vm: ViewModel) {
        counter.text = vm.counter
        image.url = vm.imageUrl
        if (state.isLoading) progress.visible() else progress.hide()
    }
}
```

Modify the `Bindings` so that it uses the `ViewModelTransformer`

```kotlin
class Bindings @Inject constructor(
    private val feature: Feature1
) {
    val binder: Binder = TODO()

    fun setup(view: View) {
        binder.bind(feature to view using ViewModelTransformer)
    }
}
```

Now the `View` doesn't care where it gets its `ViewModel` from, and can be reused to work with other data sources as well.

## Step 3: Don't emit Wish, emit a UI Event

There's one last thing that's still coupling our `View` to our `Feature1` — triggering its `Wish`es directly. Now that the `View` doesn't know where the `ViewModel` comes from, why should it talk to `Feature1` directly? All it _really_ cares about it is to provide some output. The fact that this can trigger state changes and a new `ViewModel` to render is secondary from its perspective.

Let's define our UI events as:

```kotlin
sealed class UiEvent {
    object ButtonClicked : UiEvent()
    object ImageClicked : UiEvent()
}
```

Let's remove the `Feature1` reference from our `View`, and make it a source of `UiEvent`s

```kotlin
class View(
    private val uiEvents: PublishRelay<UiEvent> = PublishRelay.create()
) : Consumer<ViewModel>, ObservableSource<UiEvent> by uiEvents {

    // remainder omitted

    private fun setupViews() {
        button.setOnClickListener { uiEvents.accept(UiEvent.ButtonClicked) }
        image.setOnClickListener { uiEvents.accept(UiEvent.ImageClicked) }
    }
}
```

Now we can connect our `View` to our `Feature1` using a transformer, much like how we did with `State` -> `ViewModel`, only this time it's in the other direction:

```kotlin
object UiEventTransformer : (UiEvent) -> Feature1.Wish? {
    override fun invoke(event: UiEvent): Feature1.Wish? = when (event) {
        is ButtonClicked -> Feature1.Wish.SetActiveButton(event.idx)
        is PlusClicked -> Feature1.Wish.IncreaseCounter
    }
}
```

```kotlin
class Bindings @Inject constructor(
    private val feature: Feature1
) {
    val binder: Binder = TODO()

    fun setup(view: View) {
        binder.bind(view to feature using UiEventTransformer)
        binder.bind(feature to view using ViewModelTransformer)
    }
}
```

## Step 4: Profit

Let's consider the benefits so far:

- We completely decoupled our UI and our business logic.
- Our `View` doesn't know anything about a `Feature`, it only knows how to render `ViewModels` and how to trigger `UiEvents`, and has become a reusable unit in itself.
- It can be fed `ViewModels` from any other source.
- Bindings, along with their lifecycle, are a separate concern.

Additionally, now that we trigger `UiEvents` in the `View`, we can bind multiple other components to it in a completely decoupled way!

Let's add an analytics tracker:

```kotlin
class AnalyticsTracker() : Consumer<UiEvent> {

    override fun accept(uiEvent: UiEvent) {
        when (uiEvent) {
            is ButtonClicked -> TODO()
            is PlusClicked -> TODO()
        }
    }
}
```

And now we can connect it with just one additional line in our `Bindings`:

```kotlin
class Bindings @Inject constructor(
    private val feature: Feature1
    private val analyticsTracker: AnalyticsTracker
) {
    val binder: Binder = TODO()

    fun setup(view: View) {
        binder.bind(view to analyticsTracker)
        binder.bind(view to feature using UiEventTransformer)
        binder.bind(feature to view using ViewModelTransformer)
    }
}
```

All this without modifying anything in our `View`!

Once you add multiple reactive components, the `Bindings` class becomes your high level overview of the whole graph of who talks to whom, in a really descriptive way.

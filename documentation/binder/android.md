# Lifecycle handling on Android

## Using AndroidBinderLifecycle directly

You can use `AndroidBinderLifecycle` with any `LifecycleOwner` to automatically end `Binder` lifecycle upon `onDestroy()`


```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binder = Binder(AndroidBinderLifecycle(this.lifecycle))
    binder.bind(a to b)
    binder.bind(c to d)
}
```


## Using AndroidBindings

Better yet, don't put your bindings into your `Activity`. Rather, take a look at `AndroidBindings`, which creates you a `Binder` automatically:

```kotlin
abstract class AndroidBindings<T : Any>(
    lifecycleOwner: LifecycleOwner
) {
    protected val binder = Binder(
        lifecycle = AndroidBinderLifecycle(
            androidLifecycle = lifecycleOwner.lifecycle
        )
    )

    abstract fun setup(view: T)
}
```

By extending this class, you can forget about `Binder` lifecycle and also extract the concern of creating your bindings:

```kotlin
// probably construct this using the DI framework of your choice:
class MyActivityBindings(
    lifecycleOwner: LifecycleOwner,
    private val feature: Feature,
    private val viewModelTransformer: ViewModelTransformer,
    private val uiEventTransformer: UiEventTransformer,
    private val analyticsTracker: AnalyticsTracker
) : AndroidBindings<MyActivity>(lifecycleOwner) {

    override fun setup(view: MyActivity) {
        binder.bind(feature to view using viewModelTransformer)
        binder.bind(view to feature using uiEventTransformer)
        binder.bind(view to analyticsTracker)
    }
}

```

Just inject it to your `Activity` and call `setup`:

```kotlin
class MyActivity : AppCompatActivity(), ObservableSource<UiEvent>, Consumer<ViewModel> {

    @Inject lateinit var bindings: MyActivityBindings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO inject
        bindings.setup(this)
    }
}
```

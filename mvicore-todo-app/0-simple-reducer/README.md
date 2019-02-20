# Todo App with MVICore

## Part 0: One screen

This tutorial focuses on creating a base for the app with minimal functionality.

This includes:
- creating a todo item
- marking it as done/not done
- deleting and item

See [the gif](writeup/img/app_example.gif) for better visualisation.

### Basic setup
The app is based on a standard Android Studio setup with an empty activity.

The [screen](src/main/res/layout/activity_main.xml) itself is fairly
simple: `EditText` and `Button` to create an item and `RecyclerView`
to show already existing ones. The [list item](src/main/res/layout/todo_item.xml) controls include functionality to mark item as done or delete it. 

### Logic
MVICore defines logic using `Feature`. They are inspired by Redux from Web,
which they share many similarities with. `Feature` provides a single source of 
truth for your logic state and ways of manipulating it from the outside world, 
keeping state consistent even in highly concurrent situations. There is a lot more
information about features and ideas behind it here(link).

Every `Feature` defines its interface. It accepts `Wish` as an input and outputs the
latest `State`. The `Wish` is usually defined as a `sealed class` and represents 
intent (`I` from MVI). The state semantics are better handled by `data class` in the 
general case, but nothing forbids you from using any entity you wish.

```kotlin
class TodoListFeature: ReducerFeature<Wish, State, News>(
  initialState = State(),
  reducer = TODO()
) {
  
}
```

Business rules can be complex, and MVICore reflects that. `Feature` can contain many 
elements, which are optional and can be added whenever needed. However, there is one 
persistent component: `Reducer`. 

Reducer handles updates of the state. It is a part that guarantees state consistency
and sequential modification. For more information, check [this page.](../../documentation/features/reducerfeature.md)



Default reducer for now which just returns the previous state.
```kotlin
object ReducerImpl : Reducer<State, Wish> {
  override fun invoke(state: State, wish: Wish): State = TODO()
}
```

### Reducer
That's where the logic implementation is. We do some stuff depending
on an input.
```kotlin
object ReducerImpl : Reducer<State, Wish> {
    override fun invoke(state: State, wish: Wish): State = when (wish) {
        is Wish.Create -> state.copy(
            todos = state.todos + wish.item.copy(id = state.nextId),
            nextId = state.nextId + 1
        )
        is Wish.Delete -> state.copy(
            todos = state.todos - wish.item
        )
        is Wish.UpdateDone -> state.copy(
            todos = state.todos.map {
                if (it.id == wish.item.id) it.copy(done = !it.done) else it
            }
        )
    }
}
```

Then we certainly need a `TodoItem` model - object representing each created item.
```kotlin
data class TodoItem(
  val id: Long = 0,
  val title: String,
  val done: Boolean = false
)
```

### View
```kotlin
data class TodoViewModel(
  val todos: List<TodoItem>
)

sealed class TodoEvent {
  data class UpdateDone(val item: TodoItem): TodoEvent()
  data class Delete(val item: TodoItem): TodoEvent()
  data class Create(val title: String): TodoEvent()
}
```

We encapsulate view logic in a `TodoListView` class. We know that is should have an input of the `TodoViewModel` and output of a `TodoEvent`. Therefore, the view implements `ObservableSource<TodoEvent>` and `Consumer<TodoViewModel>`.
```kotlin
class TodoListView(
    root: ViewGroup,
    private val events: PublishSubject<TodoEvent> = PublishSubject.create()
): ObservableSource<TodoEvent> by events, Consumer<TodoViewModel> 
```

Sending events on view interactions: 
```kotlin
submit.setOnClickListener {
    if (input.text.isNotEmpty()) {
        events.onNext(
            TodoEvent.Create(input.text.toString())
        )
        input.text.clear()
    }
}
```

Then we register listeners on a viewholder in list in a similar fashion.
```kotlin
checkBox.setOnClickListener {
    item?.let {
        events.onNext(UpdateDone(it))
    }
}
```

### Binder
Wiring all our parts together takes significant effort. We need to
transform UiEvent to Wishes and State to ViewModel. Let's try it out
in an activity.
```kotlin
val feature = TodoListFeature()
val view = TodoListView(findViewById(android.R.id.content))

Binder(CreateDestroyBinderLifecycle(lifecycle)).apply {
    bind(view to feature using UiEventToWish)
    bind(feature to view using StateToViewModel)
}
```
The transformers are just Kotlin functions, extracted to separate classes for better
visibility.
```kotlin
object UiEventToWish: (TodoEvent) -> TodoListFeature.Wish? {
    override fun invoke(event: TodoEvent): TodoListFeature.Wish? = when (event) {
        is TodoEvent.UpdateDone -> UpdateDone(event.item)
        is TodoEvent.Create -> Create(TodoItem(title = event.title))
        is TodoEvent.Delete -> Delete(event.item)
    }
}

object StateToViewModel: (TodoListFeature.State) -> TodoViewModel {
    override fun invoke(state: TodoListFeature.State): TodoViewModel =
        TodoViewModel(state.todos)
}
```

### Saving state
Android implementation uses parcelable state which is saved when `saveState` method
is called. Simple workaround is to use serializable and put the state into Android bundle. The logic of wrapping with bundle is extracted into two extension functions:

```kotlin
private fun AndroidTimeCapsule.state() =
  get<Bundle>(CAPSULE_KEY)?.getSerializable(STATE_KEY) as? State

private fun State.toParcelable() = 
  Bundle().apply { putSerializable(STATE_KEY, this@toParcelable) }
```

Then we can use much more flexible syntax defining it in the feature:

```kotlin
class TodoListFeature(
    timeCapsule: AndroidTimeCapsule
): ReducerFeature(
  initialState = timeCapsule.state() ?: State(),
  reducer = ReducerImpl
) {
  init {
    timeCapsule.register(CAPSULE_KEY) { state.toParcelable() }
  }
}
```

Final part is to attach capsule to the activity lifecycle:

```kotlin
// onCreate
capsule = AndroidTimeCapsule(savedInstanceState)
val feature = TodoListFeature(capsule)

// onSaveInstanceState
capsule.saveState(outState)
```

### Testing
(shrug)

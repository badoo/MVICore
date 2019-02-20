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
intent (`I` from MVI). The state semantics are better handled by a `data class` in 
the general case, but there are no strict rules how to define them. From the outside 
world every feature looks like a black box which is `Consumer<Wish>` and 
`ObservableSource<State>`, basic interfaces from RxJava 2. These interfaces have an 
uttermost significance for connecting MVI elements, which is explored below.

Back to the app: the todo item model contains title and state of the checkbox. The 
feature receives wishes to create an item, delete it and update "done" status. The
state stores current todos for the screen.

```kotlin
data class TodoItem(
    val id: Long = 0,
    val title: String,
    val done: Boolean = false
)

class TodoListFeature: ReducerFeature<Wish, State, Nothing>(
    initialState = State(),
    reducer = TODO()
) {
  
    sealed class Wish {
        data class Create(val item: TodoItem) : Wish()
        data class Delete(val item: TodoItem) : Wish()
        data class UpdateDone(val item: TodoItem) : Wish()
    }

    data class State(
        val nextId: Long = 0,
        val todos: List<TodoItem> = emptyList()
    )
}
```

Note `Nothing` as a third type parameter for Feature. It represents `News` in a 
signature, which are not used here. The other thing is inclusion of the `id` field 
for the `TodoItem` and `State`. These fields are used to later distiguish items among 
each other.

Business rules can be complex, and MVICore reflects that. `Feature` can be composed 
many elements, which are mostly optional and can be used only when required. However, 
there is one compulsory component: `Reducer`. 

Reducer handles updates of the state. It is a part that guarantees state consistency
and sequential modification. For more information, check [this page.](../../documentation/features/reducerfeature.md)

Here is the implementation of state update logic for the app:
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
The list is updated based on a `Wish`. Kotlin syntactic sugar allows for easy 
manipulation of the immutable state and list inside. The `nextId` field is used for 
the autoincrement functionality, similarly to how databases manage it.

> **Why immutability? Mutating the state could be more efficient, and reducer prevents concurrent modification.**

The state is still exposed to the outside world, which can easily modify it without 
any control from feature side.

### View
Similarly to the feature, view can define its own interface in terms of 
`ObservableSource` and `Consumer`. It receives `ViewModel` which represents current 
state of the view (not to be confused with MVVM) and emits results of user 
interaction as `UiEvent`. The view itself **must** be stateless and only render 
received `ViewModel` to prevent synchronization issues with business logic.

For the `TodoListView` the models are defined as follows:
```kotlin
// TodoListView.kt
data class TodoViewModel(
    val todos: List<TodoItem>
)

sealed class TodoEvent {
    data class UpdateDone(val item: TodoItem): TodoEvent()
    data class Delete(val item: TodoItem): TodoEvent()
    data class Create(val title: String): TodoEvent()
}
```
The view searches for UI elements itself in the root `ViewGroup` and registers
all the required listeners. `Consumer` implementation is simple, as it has only one 
method which handles the passed input. `ObservableSource` is implemented using Kotlin 
delegation pattern and `PublishSubject` which is already conveniently 
`ObservableSource` by itself.

```kotlin
class TodoListView(
    root: ViewGroup,
    private val events: PublishSubject<TodoEvent> = PublishSubject.create()
): ObservableSource<TodoEvent> by events, Consumer<TodoViewModel> {
    private val adapter = TodoListAdapter(events)

    override fun accept(model: TodoViewModel) {
        adapter.items = model.todos.sortedWith(TodoComparator)
    }
}
```

The view listeners are triggering events on the internal `PublishSubject` and the 
same approach is used inside of the adaptor's view holders. 
```kotlin
// TodoListView.kt
submit.setOnClickListener {
    if (input.text.isNotEmpty()) {
        events.onNext(
            TodoEvent.Create(input.text.toString())
        )
        input.text.clear()
    }
}

//TodoListAdapter.kt
checkBox.setOnClickListener {
    item?.let {
        events.onNext(UpdateDone(it))
    }
}
```
The rest of the view is pretty similar to the usual `RecyclerView` bindings. You can 
check it out [here](src/main/java/com/badoo/mvicore/todo/ui/).

### Wiring
As you may have notice above, all the elements are described in terms of 
`ObservableSource` for output and `Consumer` and input. This unification allows for
easier component wiring, which MVICore provides using `Binder`. It manages all the
subscriptions and follows provided `Lifecycle`. More in-depth explanation is available
[here.](../../documentation/binder/README.md)

First step is to define the connections between the components: view and feature.
```kotlin
// MainActivity.kt
val feature = TodoListFeature()
val view = TodoListView(findViewById(android.R.id.content))

Binder(CreateDestroyBinderLifecycle(lifecycle)).apply {
    bind(view to feature using UiEventToWish)
    bind(feature to view using StateToViewModel)
}
```
The feature output (`State`) is passed as to the view input (`ViewModel`); the view 
output (`UiEvent`) is passed to the feature input (`Wish`). Do you feel that the 
syntax describes it a bit better? 

However, these types are 
not quite compatible with each other. MVICore uses transformers (or mappers) which 
are essentially named Kotlin functions.

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
MVICore provides the mechanism to save a feature state using `TimeCapsule` hooks.
On Android it uses activity's `onSaveInstanceState` and saves feature state to the `Bundle`. When activity is restore, `Bundle` is passed to the `onCreate`, and used to construct a feature. 

```kotlin
// onCreate
capsule = AndroidTimeCapsule(savedInstanceState)
val feature = TodoListFeature(capsule)

// onSaveInstanceState
capsule.saveState(outState)
```

`TimeCapsule` operates with `Parcelable`, which can be easily implemented for most of
models. However, the implementation can look a bit bulky, so I abused the fact that
`Bundle` is `Parcelable`, and used Java serialization.

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

private fun AndroidTimeCapsule.state() =
  get<Bundle>(CAPSULE_KEY)?.getSerializable(STATE_KEY) as? State

private fun State.toParcelable() = 
  Bundle().apply { putSerializable(STATE_KEY, this@toParcelable) }
```

### Testing
(shrug)

# Efficient view updates

MVICore includes utility class to observe difference in the received models and prevent redundant view updates.

```kotlin
data class ViewModel(
    val buttonText: String,
    val buttonAction: () -> Unit,
    val isLoading: Boolean
)

class View: Consumer<ViewModel> {

    private val button: Button = ...
    
    // Specify the fields to observe and actions to execute
    private val watcher = modelWatcher<ViewModel> {
        watch(ViewModel::buttonText) {
            button.text = it
        }
        watch(ViewModel::buttonAction, diff = byRef()) {
            button.setOnClickListener { it() }
        }
    }
    
    override fun accept(model) {
        // Pass the model
        watcher.invoke(model)
    }
}
```
    
By default, the difference is calculated by value (using `equals`). It is configurable through `diff` parameter.
The library also includes a couple of commonly used defaults.

```kotlin
val watcher = modelWatcher<Model> {
    watch(Model::field, diff = byValue()) {  } // Compare using equals (default strategy)
    watch(Model::field, diff = byRef()) { }    // Compare using referential equality   
}
```

The difference can be observed on more than one field with custom diff strategy. 
For example, if the click listener should not be set when something is loading, you can do the following:
```kotlin
// Trigger when either loading flag or action changed
val loadingOrAction: DiffStrategy<ViewModel> = { p1, p2 ->
    p1.isLoading != p2.isLoading || p1.buttonAction !== p2.buttonAction
}

val watcher = modelWatcher<ViewModel> {
    watch({ it }, diff = loadingOrAction) { model ->
        // Allow action only when not loading
        button.setOnClickListener(
            if (!model.isLoading) model.buttonAction else null
        )
    }
}
```

Models based on the sealed classes are supported with `type` and `objectType` functions.
```kotlin
sealed class Model {
    data class A(val list: List<String>): Model()
    object B : Model()
}

val watcher = modelWatcher<Model> {
    type<A> {
        watch(Model.A::list) { }
    }
    
    objectType<B> { modelB ->
        
    }
}
```

!!! warning 
    Subsequent definitions of the same type will override previous ones.

If sealed class has a common property defined in the base class, its changes can be observed as well.
In the example below, `Model::list` selector is triggered when the property is changed independently on model type.
```kotlin
sealed class Model {
    abstract val list: List<String>

    data class A(val list: List<String>): Model()
    object B : Model() {
        override val list: List<String> = emptyList()
    }
}

val watcher = modelWatcher<Model> {
    type<A> {
        watch(Model.A::list) { 
            // Property of Model.A only
        }
    }
 
    watch(Model::list) {
        // Common property
    }
}
```

The watcher also provides an optional DSL to add more clarity to the definitions:
```kotlin
val watcher = modelWatcher<ViewModel> {
    // Method call
    watch(ViewModel::buttonText) {
        button.text = it
    }
    
    // DSL
    ViewModel::buttonText {
       button.text = it
    }
}
```
The same applies to custom strategies.
```kotlin
val watcher = modelWatcher<ViewModel> {
    // Method call
    watch(Model::buttonAction, diff = byRef()) { }
    
    // DSL
    val byRef = byRef<() -> Unit>()
    ViewModel::buttonAction using byRef {
    
    }
}
```
Common strategies on two fields can be defined in a simpler way.
```kotlin
val watcher = modelWatcher<ViewModel> {
    // Method call
    val loadingOrAction: DiffStrategy<ViewModel> = { p1, p2 ->
        p1.isLoading != p2.isLoading || p1.buttonAction !== p2.buttonAction
    }
    watch({ it }, loadingOrAction) {
    
    }

    // DSL
    (ViewModel::isLoading or ViewModel::buttonAction) {
        
    }
}
```

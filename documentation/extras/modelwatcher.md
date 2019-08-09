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
        watch(ViewModel::buttonAction, diffStrategy = byRef()) {
            button.setOnClickListener { it() }
        }
    }
    
    override fun accept(model) {
        // Pass the model
        watcher.invoke(model)
    }
}
```
    
By default, the difference is calculated by value (using `equals`). It is configurable through `diffStrategy` parameter.
The library also includes a couple of commonly used defaults.

```kotlin
val watcher = modelWatcher<Model> {
    watch(Model::field, diffStrategy = byValue()) {  } // Compare using equals (default strategy)
    watch(Model::field, diffStrategy = byRef()) { }    // Compare using referential equality   
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
    watch({ it }, diffStrategy = loadingOrAction) { model ->
        // Allow action only when not loading
        button.setOnClickListener(
            if (!model.isLoading) model.buttonAction else null
        )
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
    watch(Model::buttonAction, diffStrategy = byRef()) { }
    
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

## Calculating model diff

MVICore includes utility classes to observe difference in the received models and prevent redundant view updates.

```kotlin
data class ViewModel(
    val buttonText: String,
    val buttonAction: () -> Unit
)

class View: Consumer<ViewModel> {

    private val button: Button = ...
    
    // Specify the fields to observe and actions to execute
    private val watcher = modelWatcher {
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
val watcher = modelWatcher {
    watch(Model::field, diffStrategy = byValue()) {  } // Compare using equals (default strategy)
    watch(Model::field, diffStrategy = byRef()) { }    // Compare using referential equality   
}
```

The difference can be observed on more than one field with identity function as accessor and custom diff strategy.
```kotlin
fun bothFieldsChange() = { p1, p2 ->
    p1.text == p2.text && p1.buttonAction === p2.buttonAction
}

val watcher = modelWatcher {
    watch({ it }, diffStrategy = bothFieldChange()) { model ->
        button.buttonText = model.buttonText
        button.setOnClickListener { model.buttonAction() }
    }
}
```

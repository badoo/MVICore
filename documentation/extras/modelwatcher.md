## Calculating model diff

MVICore includes utility classes to observe difference in the received models to prevent redundant view updates.

```kotlin
data class Model(
    val text: String,
    val action: () -> Unit
)

class View: Consumer<Model> {

    private val button: Button = ...
    
    // Specify the fields to observe and actions to execute
    private val watcher = modelWatcher {
        watch(Model::text) {
            button.text = it
        }
        watch(Model::action, diffStrategy = byRef()) {
            button.setOnClickListener { it() }
        }
    }
    
    override fun accept(model) {
        // Pass the model
        watcher.invoke(model)
    }
}
```
    
By default, the difference is calculated by value (using `equals`). You can specify your own whenever needed.
The library also includes a couple of commonly used default values.

```kotlin
val watcher = modelWatcher {
    watch(Model::field, diffStrategy = byValue()) {  } // Compare using equals (default strategy)
    watch(Model::field, diffStrategy = byRef()) { }    // Compare using referential equality   
}
```

The difference can be observed on more than one field with identity function as accessor and custom diff strategy.
```kotlin
fun bothFieldsChange() = { p1, p2 ->
    p1.text == p2.text && p1.action === p2.action
}

val watcher = modelWatcher {
    watch({ it }, diffStrategy = bothFieldChange()) { model ->
        button.text = model.text
        button.setOnClickListener { model.action() }
    }
}
```

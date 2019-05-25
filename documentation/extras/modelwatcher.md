## Calculating model diff

MVICore includes utility classes to observe difference in the received models,
which can be used to prevent redundant updates.

```kotlin
// Specify the fields to observe and actions to execute
val watcher = modelWatcher {
    watch(Model::field) {
        doStuff(it)
    }
    watch(Model::listField, diffStrategy = byRef()) {
        updateList(it)
    }
}

// Pass the model
watcher.invoke(model)
```

By default, the difference is calculated by value (using `equals`). You can specify your own whenever needed.
The library also includes a couple of commonly used default values.

```kotlin
val watcher = modelWatcher {
    watch(Model::field, diffStrategy = byValue()) {  } // Compare using equals (default strategy)
    watch(Model::field, diffStrategy = byRef()) { }    // Compare using referential equality   
    
    // Defining custom strategy
    fun twoFieldsChange(): DiffStrategy<Model> = { m1, m2 -> 
        m1.field == m2.field && m1.listField === m2.listField
    }
    // itself() returns the model when more than one field is needed
    watch(itself(), diffStrategy = twoFieldsChange()) { } 
}
```
---

[Go up one level](README.md)

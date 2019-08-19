# Feature to Feature communication

## Don't

At the initial launch of this library, the suggested way to make two `Features` communicate with each other was to inject one in the constructor of the other.

This was before we elaborated on the `Binder`, and it's not just no longer suggested, but it's straight out considered an **anti-pattern**.

!!! warning ""
    Injecting them has many disadvantages:
    
    1. They become coupled
    2. Scoping becomes a potential problem (it works only as long the injected one is *always* supposed to outlive the other one)
    3. If you need two-way connection between them, it manifests itself as a circular dependency   


## Do

Use `Binder` bindings to connect `Features` as described in the [relevant chapter](../../binder/binder/):

```kotlin
binder.bind(feature1.news to feature2 using NewsToWish)
```

!!! note ""
    This is considered superior to the above injection-based approach in all aspects:
    
    1. `Features` can stay completely decoupled from one another
    2. Scoping is automatically handled by `Binder`
    3. `Binder` can even resubscribe a connection automatically if needed
    4. In the case of a two-way connection, you can do it without a circular dependency, by adding a second binding. 



  

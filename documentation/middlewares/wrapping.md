# Automatic wrapping of reactive components with Middlewares

Previous: [1. What is a middleware and why is it good for me?](middleware.md)

Next : [3. Middleware configurations](configuration.md)

[Go up one level](README.md)

## The power of `Consumer<T>`

Remember when in the [Core concepts](../features/coreconcepts.md) we said that
`Feature` is a `Consumer` of `Wish` and an `ObservableSource` of `State`? And that in general, the framework is working with outputs of type `ObservableSource<T>` and inputs of type `Consumer<T>`?

Now this comes really handy.

There's a Kotlin extension method to wrap any `Consumer<T : Any>` object with `Middlewares`:

```kotlin
val target: Consumer<T> = TODO()

// without name
target.wrap()

// or with name
target.wrap("Target input")
```

In essence, you can add `Middlewares` to just about anything you want if it implements `Consumer<T>`.

## What Middlewares?

The list of `Middlewares` that will be applied can be customised flexibly by name, package name, and lots of other conditions (including your own) as we we'll see in the next chapter: [3. Middleware configurations](configuration.md)

## Middlewares for Features - out of the box

As an extra, the `BaseFeature` implementation is also using `Consumer<T>` internally for its components.

That means, you can add `Middlewares` to not just the `Reducer`, but the `Actor`, `Bootstrapper`, `PostProcessor`, and `NewsPublisher` as well. These wrappings will be named:
- com.example.myapp.MyCoolFeature.BootstrapperImpl.output
- com.example.myapp.MyCoolFeature.ActorImpl.input
- com.example.myapp.MyCoolFeature.ReducerImpl.input
- com.example.myapp.MyCoolFeature.PostProcessorImpl.input
- com.example.myapp.MyCoolFeature.NewsPublisherImpl.input

## Middlewares for bindings - out of the box

Whenever you create bindings, you get free `Middlewares`:

```kotlin
binder.bind(source to target)
binder.bind(source to target using transformer)
```

Since `target` here is always a `Consumer<T>`, `Binder` can automatically invoke `.wrap()` on it.

These wrappings will have a name containing the connection name, the source and the target: `ANONYMOUS (SourceObject.toString() --> TargetObject.toString()")`.

If you want descriptive names for the connection part instead of ANONYMOUS, remember that you can use your own:

```kotlin
binder.bind(source to target named "MyTarget.ViewModels")
binder.bind(source to target using transformer ""MyTarget.ViewModels")
```


---

Next : [3. Middleware configurations](configuration.md)

[Go up one level](README.md)


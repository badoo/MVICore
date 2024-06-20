## Changelog

### Pending changes

([#195](https://github.com/badoo/MVICore/pull/195)):
Updated Kotlin to 1.9.23

([#193](https://github.com/badoo/MVICore/pull/193)):
Updated Kotlin to 1.8.10

([#194](https://github.com/badoo/MVICore/pull/194)):
Introduced the ability to override `AndroidMainThreadFeatureScheduler` via `MviCoreAndroidPlugins` (similar to `RxAndroidPlugins` within RxAndroid).

### 1.4.0

#### Additions

([#179](https://github.com/badoo/MVICore/pull/179)):
Introduced the ability to specify observation scheduler within the `Binder` class (see the `observeOn` DSL below), as well as the `observeOn` infix operator for `Connection` class (related to `Binder`).

An example of how you might use this is as follows:

```kotlin
// mvicore-android example
testLifecycleOwner.lifecycle.createDestroy {
    observeOn(mainScheduler) {
        bind(events to uiConsumer1)
        bind(events to uiConsumer2)
    }
    observeOn(backgroundScheduler) {
        bind(events to backgroundConsumer1)
        bind(events to backgroundConsumer2)
    }
    bind(events to uiConsumer3 observeOn mainScheduler)
    bind(events to backgroundConsumer3 observeOn backgroundScheduler)
}

// binder example
binder.observeOn(mainScheduler) {
    bind(events to uiConsumer1)
    bind(events to uiConsumer2)
}
```

See more details in [advanced binder](../binder/binder-advanced/#setting-connections-observation-scheduler) section.

([#177](https://github.com/badoo/MVICore/pull/177)):
Updated AndroidX appcompat to 1.4.1 and lifecycle to 2.5.1. Also updated Compile and Target SDK to API 33.

([#176](https://github.com/badoo/MVICore/pull/176)):
Updated RxJava to 2.2.21 and RxKotlin to 2.4.0

([#173](https://github.com/badoo/MVICore/pull/173)):
Updated Kotlin to 1.7.10

([#162](https://github.com/badoo/MVICore/pull/162)):
Updated Kotlin to 1.6.21 (the plan is to update to 1.7.x fairly soon)

### 1.3.1

#### Bug fixes

([#138](https://github.com/badoo/MVICore/issues/167)):
Fixed regression related to BaseFeature actor.

The Actor subject was made serializable, and was also using a flatMap. Both of these changes caused a change in behaviour relating to the ordering of news (in features that have a PostProcessor which triggers extra actions).
This change was made as part of introducing the optional `FeatureScheduler` to `BaseFeature`.

If you provide a `FeatureScheduler` and use a PostProcessor, please be aware that the ordering of your news could change.

The previous news ordering behaviour is actually a bug in BaseFeature caused by recursion, and will hopefully be addressed (as an opt in change) in a future release.

### 1.3.0

#### Additions

([#147](https://github.com/badoo/MVICore/pull/147)):
Introduced 'async' feature which is moves work to a dedicated single-threaded `feature scheduler`, while being observable on the `observation scheduler`

([#148](https://github.com/badoo/MVICore/pull/148)):
Updated mockito-kotlin library.

([#150](https://github.com/badoo/MVICore/pull/150)):
Fixed Bootstrapper variance.

([#150](https://github.com/badoo/MVICore/pull/155)):
Minor improvements to the new 'async' feature.

([#158](https://github.com/badoo/MVICore/pull/158)):
Introduced an optional `FeatureScheduler` which can be used with non-async features.
This is useful when a feature is instantiated on a thread other than the thread it should be bound to (for example the UI thread).

When providing a `FeatureScheduler`, the feature is able to correctly switch to its desired thread rather than throwing an exception.

([#160](https://github.com/badoo/MVICore/pull/160)):
Changed to Java 8 compatibility

([#161](https://github.com/badoo/MVICore/pull/161)):
Improved the error message thrown by `SameThreadVerifier`. It now includes the feature's class name and the thread names.

### 1.2.6

#### Bug fixes

([#138](https://github.com/badoo/MVICore/pull/138)):
Generated templates plugin to be able to work on AndroidStudio 4.1. Fixes ([#135](https://github.com/badoo/MVICore/issues/135)).

### 1.2.5

#### API changes

([#134](https://github.com/badoo/MVICore/pull/134)):
Migrated project to AndroidX.

([#129](https://github.com/badoo/MVICore/pull/129)):
Extracted binder from mvicore module. To continue using it, please add this additional dependancy: `com.github.badoo.mvicore:binder:x.x.x`.

### 1.2.4

#### API changes

([#112](https://github.com/badoo/MVICore/pull/112)):
Update model watcher dsl to support sealed classes.

#### Additions

([#113](https://github.com/badoo/MVICore/pull/113)):
Updated inspector plugin Android Studio version to 2019.2.

### 1.2.3

#### Additions

([#106](https://github.com/badoo/MVICore/pull/106)):
Improve type signatures for `ModelWatcher`. This does not impact the existing API.

### 1.2.2

#### Dependency changes

([#102](https://github.com/badoo/MVICore/pull/102)):
Changed the gradle artifact group id from `com.github.badoo` to `com.github.badoo.mvicore`
Ensure that you update your gradle build files (i.e. `com.github.badoo.mvicore:binder:x.x.x`)

### 1.2.1

#### Bug fixes

([#98](https://github.com/badoo/MVICore/pull/98)):
Fix issues with inspector plugin

([#100](https://github.com/badoo/MVICore/pull/100)):
Fix 1.2.0 debug drawer build

### 1.2.0

#### API changes

([#66](https://github.com/badoo/MVICore/pull/66)):
`Connection` is updated to provide information about both `ObservableSource` and `Consumer` types.
`ConsumerMiddleware<In>` is replaced by `Middleware<Out, In>` to provide access to both input and output types of the `Connection`.

#### Additions

([#73](https://github.com/badoo/MVICore/pull/73)):
Allows transformer to access to the stream between `Source` and `Consumer`. See more details in 
[advanced binder](../binder/binder-advanced/#changing-reactive-chain-between-input-and-output) section.

([#76](https://github.com/badoo/MVICore/pull/76)):
`MemoFeature` which keeps latest accepted state.

([#82](https://github.com/badoo/MVICore/pull/82)):
Organized way of diffing fields in model to provide more efficient view updates. More information [here](../extras/modelwatcher/).

([#77](https://github.com/badoo/MVICore/pull/77)):
Idea plugin to observe elements of connections based on middlewares. Read about it [here](../extras/inspectorplugin/#android-studio-plugin-to-observe-elements-of-connections).




# MVICore 
[![Build Status](https://travis-ci.org/badoo/MVICore.svg?branch=master)](https://travis-ci.org/badoo/MVICore)

## What's this?

MVICore is a 100% Kotlin, modern MVI framework featuring:
- an easy way to implement your business features in a reactive way with unidirectional dataflow
- binding them to the UI with lifecycle handling
- middlewares
- time travel debugger

Head over to the [Documentation](documentation/README.md) to see what's possible.

## Version

Latest version is `1.0.0`

## Download

Available through jitpack.

Add the maven repo to your root `build.gradle`

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependencies:
- Framework:
```groovy
implementation 'com.github.badoo.mvicore:mvicore:{latest-version}'
```

- Helper classes for Android:
```groovy
implementation 'com.github.badoo.mvicore:mvicore-android:{latest-version}'
```

- Time travel debugger controls in a [DebugDrawer](https://github.com/palaima/DebugDrawer) module
```groovy
implementation 'com.github.badoo.mvicore:mvicore-debugdrawer:{latest-version}'
```

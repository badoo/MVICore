# MVICore 
[![Build Status](https://travis-ci.org/badoo/MVICore.svg?branch=master)](https://travis-ci.org/badoo/MVICore)

## What's this?

MVICore is a modern MVI framework featuring:
- **100% Kotlin**: An easy way to implement your business features in a reactive way with unidirectional dataflow
- **Scaling with complexity**: operate with a single Reducer if needed, with the option of having the full power of additional components to handle more complex cases
- **Event handling**: A solution to handling events that you don’t want to store in the state
- **Reactive component binding**: A super simple API to bind your reactive endpoints to each other with automatic lifecycle handling
- **Custom Middlewares**: for every single component in the system, with flexible configuration options
- **Logger**: An out-of-the-box logger Middleware
- **Time Travel Debugger**: for ALL of your reactive components (not just your state machine!) with UI controls for recording and playback

## Documentation

Head over to the [Documentation](documentation/README.md) to see what's possible.

## Version

Latest version is `1.1.2`

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

- Time Travel Debugger controls in a [DebugDrawer](https://github.com/palaima/DebugDrawer) module
```groovy
implementation 'com.github.badoo.mvicore:mvicore-debugdrawer:{latest-version}'
```

# MVICore
[![Version](https://jitpack.io/v/badoo/mvicore.svg)](https://jitpack.io/#badoo/mvicore)
[![Build Status](https://travis-ci.org/badoo/MVICore.svg?branch=master)](https://travis-ci.org/badoo/MVICore)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## What's this?

MVICore is a modern, Kotlin-based MVI framework:
- **Scaling with complexity**: operate with a single Reducer if needed, with the option of having the full power of additional components to handle more complex cases
- **Event handling**: A solution to handling events that you don’t want to store in the state
- **Reactive component binding**: A super simple API to bind your reactive endpoints to each other with automatic lifecycle handling
- **Custom Middlewares**: for every single component in the system, with flexible configuration options
- **Logger**: An out-of-the-box logger Middleware
- **Time Travel Debugger**: for ALL of your reactive components (not just your state machine!) with UI controls for recording and playback


## Documentation

The library comes with lots of powerful capabilities and tooling.

See [https://badoo.github.io/MVICore/](https://badoo.github.io/MVICore) for full documentation.

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

- ModelWatcher for efficient view updates
```groovy
implementation 'com.github.badoo.mvicore:mvicore-diff:{latest-version}'
```

- Time Travel Debugger controls in a [DebugDrawer](https://github.com/palaima/DebugDrawer) module (You need to add the dependencies to DebugDrawer and configure it yourself before you can use this):
```groovy
implementation 'com.github.badoo.mvicore:mvicore-debugdrawer:{latest-version}'
```

## Related articles & videos
- [MVI beyond state reducers](https://badootech.badoo.com/a-modern-kotlin-based-mvi-architecture-9924e08efab1)
- [Building a system of reactive components with Kotlin](https://badootech.badoo.com/building-a-system-of-reactive-components-with-kotlin-ff56981e92cf)
- [Unidirectional data-flow and the Zen of black box components](https://medium.com/p/unidirectional-data-flow-and-the-zen-of-black-box-components-fff5d618f8b6?source=email-e819b9e65829--writer.postDistributed&sk=e17b031a4f155a8dc7d3248489116240)
- [Time Travel Debug Everything!](https://badootech.badoo.com/time-travel-debug-everything-droidconuk-2018-lightning-talk-445217258401)

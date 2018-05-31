# MVICore 
[![Build Status](https://travis-ci.org/badoo/MVICore.svg?branch=master)](https://travis-ci.org/badoo/MVICore)

## What's this?

MVICore is a 100% Kotlin, modern MVI framework featuring:
- an easy way to implement your business features in a reactive way with unidirectional dataflow
- binding them to the UI with lifecycle handling
- middlewares (_Coming soon_)
- time travel debugger (_Coming soon_)

Head over to the [Documentation](documentation/README.md) to see what's possible.

## Version

Latest version is `0.6`

It is considered production ready already, but even though we most probably won't change current APIs until `1.0`, there's no guarantee to that.

## Download

Available through jitpack:

1. Add the maven repo to your root `build.gradle`

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency

```groovy
implementation 'com.github.badoo:MVICore:{latest-version}'
```

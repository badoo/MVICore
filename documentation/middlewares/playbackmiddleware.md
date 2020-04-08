# PlaybackMiddleware - The engine of the Time Travel Debugger

## Time Travel Debugging - out of the box

MVICore comes with a configurable `PlaybackMiddleware` that you can use right away.

```kotlin
val playbackMiddleware = PlaybackMiddleware(
        wrapped = consumer,
        recordStore = recordStore,
        logger = { System.out.println(it) } // optional
    )
```

The parameter `recordStore` should be an instance of `PlaybackMiddleware.RecordStore`.

There's currently one implementation inside the library for that:

```kotlin
val recordStore = MemoryRecordStore(
    playbackScheduler = AndroidSchedulers.mainThread(),
    logger = { System.out.println() } // optional
)
```

Since this is an in-memory implementation, it will not survive app crashes. This makes it somewhat limited, but for most cases it should be enough (when you have a crash, at least you have a stacktrace - the more difficult cases are when you don't, but still have to figure out what's going wrong). Implementations that record to a persistent storage / network are on the roadmap (contributions welcome).

## How to use

#### Setup

1. Create your `RecordStore` instance
2. Create your `PlaybackMiddleware` factory, passing in your `RecordStore`
3. Add a rule when your middleware should be used as seen in [Middleware configurations](configuration.md)
4. You can either call `.wrap()` on a `Consumer<T>` or let `Binder` do it automatically as seen in [Automatic wrapping of reactive components with Middlewares](wrapping.md)

#### The manual way

Create a record manually:

1. Holding on to your `RecordStore` instance (should be the same reference you passed to your `PlaybackMiddleware`), call `.startRecording()` on it. This will record all elements that pass through on any of the `Consumer<T>` instances wrapped `PlaybackMiddleware`.
2. Call `.stopRecording()` when you are done.

Replay a record manually:

1. Get a list of recorded and available channels from your `RecordStore` by calling `fun records(): Observable<List<RecordKey>>` on it. This is an `Observable` as if any of the channels are disposed in the meantime, they are removed from the `RecordStore` to prevent memory leaks.
2. Select a `RecordKey` from the list. You can use its `name` field to find the one you are looking for - it should contain the name of the wrapping you used when you called a `consumer.wrap("Name")` or when you created a binding using `binder.bind(source to target named "Name")
2. Call `playback(recordKey)` on `RecordStore`.

#### The automatic way
If you are on Android and using [DebugDrawer](https://github.com/palaima/DebugDrawer), you can find a UI control module you can add to it in the `mvicore-debugdrawer` dependency.

![MVICore DebugDrawer module](https://i.imgur.com/AXfyo9r.png)

UI controls include:

- start recording
- stop recording
- start playback
- record selection dropdown, which automatically updates itself with all records available in the RecordStore

## A word about playback

As said, `RecordStore` will record all channels, but can only play back one channel at a time, which you have to select (either with the UI controls, or with a `RecordKey` from code).

This is by design. Imagine a case, where you have (A), (B), (C) components wired in a way that any element passed to (A) will trigger a chain reaction that trickles down all the way to UI.

```kotlin
// input --> (A) --> (B) --> (C) --> UI

binder.bind(input to A)
binder.bind(A to B)
binder.bind(B to C)
binder.bind(C to UI)
```

Let's assume we add `PlaybackMiddleware` on all the right ends of the arrows to record elements.

If you play back elements to (A), it will trigger all inputs to (B), which in turn will trigger (C), which in turn will talk to the UI, as a result of the chain reaction.

If you played back elements simultaneously on all channels:

- (A) would be fine receiving its own playback
- (B) would receive 2x elements: both from its own playback, and ones triggered from (A)'s playback
- (C) would receive 3x elements: elements from its own playback, elements triggered from (B)'s playback, and elements triggered from (A)'s playback that triggered new elements in (B)
- UI would receive 4x as many elements as intended

Limiting playback to one channel is still enough for you to play detective when something goes wrong:

1. Attach a runtime debugger, set a breakpoint, play back input channel for that component and check what's happening there.
2. If the problem is not there, pick another component and play back elements to its input.
3. Rinse and repeat.

You might have cases where you are sure that you could replay multiple channels simultaneously that wouldn't affect each other (e.g. navigation events and view models rendered). Support for multi-channel playback in such cases will be added in later versions.

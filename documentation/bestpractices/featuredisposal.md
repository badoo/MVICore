# Feature disposal

Never forget to call `feature.dispose()` when the context your `Feature` is living in goes away!

!!! note ""
    A `Feature` is essentially a hot observable. It's neither invoked nor scoped by any subscribers to it. When its lifetime should end, you need to make sure any internal subscriptions it holds are released.

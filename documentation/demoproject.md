## Example app

You can check out the `mvicore-demo-app` module and deploy it to your Android device to see some of the possibilities of MVICore applied in practice.

![Demo app screenshot](https://i.imgur.com/O7N7Wef.png)&nbsp;&nbsp;&nbsp;
![MVICore DebugDrawer module](https://i.imgur.com/AXfyo9r.png)

Points of interest:
- `Feature1`: Simple state machine holding on to state of coloured buttons and counter
- `Feature2`: Asynchronous state machine responsible for loading images from the [The Cat API](https://thecatapi.com/)
- `MainActivityBindings`: Showcasing `Binder` usage, combining the state of `Feature1` + `Feature2` and translating them to a `ViewModel` to the screen, also connecting the UI to the Features
- `App.kt` for Middleware configurations
- Dagger configuration to make `Feature1` and `Feature2` live longer than the Activity, but lose their state on "sign out / sign in" cycle
- Check out the DebugDrawer module (swipeable from the right side of the screen) for Time Travel Debugger controls.

---

[Go up one level](../README.md)

## Android Studio plugin to observe elements of connections

!!! note
    This plugin is still under development (**requires MVICore 1.2.0 or later**)

Allows to record elements passed through middlewares and inspect their values.

### How to use
1. Download [MVICoreInspector.zip](https://github.com/badoo/MVICore/blob/master/mvicore-plugin/idea/artifacts/MVICoreInspector.zip?raw=true)
2. Inside the IDE go to "Install plugin from disk..."
3. Install the .zip file and restart the IDE
4. Add plugin middleware artifact to your dependencies: `implementation "com.github.badoo.mvicore:mvicore-plugin-middleware:$VERSION"`
5. Setup middleware configuration:
    ```kotlin
    // Create store
    val store = DefaultPluginStore(BuildConfig.APPLICATION_ID)
    
    // Apply middleware
    Middlewares.configurations.add(
         MiddlewareConfiguration(
             condition = WrappingCondition.Always,
             factories = listOf(
                 { consumer -> IdeaPluginMiddleware(consumer, store) }
             )
         )
    )
    ```
6. Don't forget to add permission in AndroidManifest.xml: `<uses-permission android:name="android.permission.INTERNET" />`. The plugin uses socket connection, which will not work without it.
7. Connect device, so it is visible to the IDE. Run your app.
8. Once app is running, press Run icon in the MVICore panel.

![Screenshot](https://i.imgur.com/Vjk0NZl.png)

The pane contains three sections:

- Left - List of events passed through middlewares.
- Right top - List of active connections. You can filter events by selecting connections.
- Right bottom - Currently selected event from the left part. Displays event data and connection it was sent from.

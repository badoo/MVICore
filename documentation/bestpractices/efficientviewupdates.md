# Efficient view updates

If you have complex view models, you probably don't want to re-render the whole UI just because one field has changed. Even though usually it's not a big issue, in extreme cases, this can cause performance problems.
 
MVICore comes with a tool called `ModelWatcher` that addresses this issue. It allows you to split your view model stream by fields and update only relevant parts of your UI, when those fields have actually changed.

See more info in [relevant section under extras](../../extras/modelwatcher).

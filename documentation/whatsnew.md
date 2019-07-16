## Changelog

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




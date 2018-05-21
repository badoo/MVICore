package com.badoo.mvicore.featurewithaction.implementation

import com.badoo.mvicore.feature.DefaultFeature
import com.badoo.mvicore.featurewithaction.implementation.SimpleFeatureAdapter.Action
import io.reactivex.Observable

/**
 * Any class extending this as an adapter doesn't have to supply a WishMapper.
 *
 * The default implementation is to wrap whatever Wish comes in in Action.Execute,
 * then unwrap it inside ExecutingActor and pass it to the implementation Actor.
 *
 * See SimpleFeature how in practice this makes it look just the same as before.
 */
abstract class SimpleFeatureAdapter<Wish : Any, Effect : Any, State : Any>(
    initialState: State,
    actor: Actor<State, Wish, Effect>,
    reducer: Reducer<State, Effect>
) : DefaultFeature<Wish, Action<Wish>, Effect, State>(
    initialState = initialState,
    wishMapper = WishMapperImpl<Wish>(),
    actor = ExecutingActor<State, Wish, Effect>(
        actor
    ),
    reducer = reducer
) {
    sealed class Action<Wish : Any> {
        data class Execute<Wish : Any>(val wish: Wish) : Action<Wish>()
    }

    class WishMapperImpl<Wish : Any> :
        WishMapper<Wish, Action<Wish>> {
        override fun invoke(wish: Wish): Action<Wish> =
            Action.Execute(
                wish
            )
    }

    class ExecutingActor<State : Any, Wish : Any, Effect : Any>(
        private val actor: Actor<State, Wish, Effect>
    ) : Actor<State, Action<Wish>, Effect> {
        override fun invoke(state: State, action: Action<Wish>): Observable<Effect> = when (action) {
            is Action.Execute<Wish> -> actor.invoke(state, action as Wish)
        }
    }
}

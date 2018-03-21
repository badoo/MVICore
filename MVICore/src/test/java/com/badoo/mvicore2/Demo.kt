package com.badoo.mvicore2

import com.badoo.mvicore2.Demo.DemoEffect.Addition
import com.badoo.mvicore2.Demo.DemoEffect.InProgress
import com.badoo.mvicore2.Demo.DemoEffect.Multiplication
import com.badoo.mvicore2.Demo.DemoEvent.Type.ADD
import com.badoo.mvicore2.Demo.DemoEvent.Type.MULTIPLY
import com.badoo.mvicore2.Demo.DemoViewModel.Loading
import com.badoo.mvicore2.Demo.DemoViewModel.Value
import com.badoo.mvicore2.Demo.DemoWish.Add
import com.badoo.mvicore2.Demo.DemoWish.Multiply
import com.badoo.mvicore2.binder.Transformer
import com.badoo.mvicore2.store.Reducer.Actor
import com.badoo.mvicore2.store.Reducer.Effect
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.rxkotlin.cast

class Demo {

    // region view interface

    data class DemoEvent(val number: Int, val type: Type) {
        enum class Type {
            ADD,
            MULTIPLY
        }
    }

    sealed class DemoViewModel{
        object Loading : DemoViewModel()
        data class Value(val value : Int) : DemoViewModel()
    }

    // endregion

    // region store interface

    sealed class DemoWish {
        data class Add(val value: Int) : DemoWish()
        data class Multiply(val factor: Int) : DemoWish()
    }

    data class DemoState(val value: Int = 0, val loading: Boolean = false)

    // endregion

    // region transformers

    object DemoEventTransformer : Transformer<DemoEvent, DemoWish> {

        override fun invoke(event: DemoEvent): DemoWish? = when (event.type) {
            ADD -> Add(event.number)
            MULTIPLY -> Multiply(event.number)
        }
    }

    object DemoStateTransformer : Transformer<DemoState, DemoViewModel> {

        override fun invoke(state: DemoState): DemoViewModel = if(state.loading) Loading else Value(state.value)
    }

    object NewsTransformer : Transformer<Unit, DemoWish> {
        override fun invoke(news: Unit): DemoWish = Add(1)
    }

    // endregion

    // region store impl

    class DemoActor : Actor<DemoWish, DemoState> {
        override fun invoke(wish: DemoWish, currentState: DemoState): Observable<Effect<DemoState>> = when (wish) {
            is Add -> add(wish)
            is Multiply -> multiply(wish)
        }

        private fun multiply(wish: Multiply): Observable<Effect<DemoState>> =
                just(Multiplication(wish.factor)).cast<DemoEffect>()
                        .startWith(InProgress).cast()

        private fun add(wish: Add): Observable<Effect<DemoState>> =
                just(Addition(wish.value)).cast<DemoEffect>()
                        .startWith(InProgress).cast()
    }

    interface DemoEffect : Effect<DemoState> {

        data class Addition(private val increment: Int) : DemoEffect {
            override fun invoke(oldState: DemoState): DemoState = oldState.copy(
                    value = oldState.value + increment,
                    loading = false
            )
        }

        data class Multiplication(private val factor: Int) : DemoEffect {
            override fun invoke(oldState: DemoState): DemoState = oldState.copy(
                    value = oldState.value * factor,
                    loading = false
            )
        }

        object InProgress : DemoEffect {
            override fun invoke(oldState: DemoState): DemoState = oldState.copy(loading = true)
        }
    }

    // endregion
}

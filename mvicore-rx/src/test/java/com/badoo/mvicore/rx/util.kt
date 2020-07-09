package com.badoo.mvicore.rx

import com.badoo.mvicore.rx.element.Actor
import com.badoo.mvicore.rx.element.Bootstrapper
import com.badoo.mvicore.rx.element.NewsPublisher
import com.badoo.mvicore.rx.element.Reducer
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer
import org.junit.Assert.assertEquals

open class TestConsumer<T>: Consumer<T> {
    val values = mutableListOf<T>()

    override fun accept(value: T) {
        values += value
    }
}

fun <T> TestConsumer<T>.assertValues(vararg values: T) =
    assertEquals(values.toList(), this.values)

fun <T> TestConsumer<T>.assertNoValues() =
    assertEquals(emptyList<T>(), this.values)

fun <State, Effect> reducer(block: (state: State, effect: Effect) -> State) =
    object : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = block(state, effect)
    }

fun <State, Wish, Effect> actor(block: (state: State, wish: Wish) -> ObservableSource<out Effect>) =
    object : Actor<State, Wish, Effect> {
        override fun invoke(state: State, action: Wish): ObservableSource<out Effect> =
            block(state, action)
    }

fun <Action> bootstrapper(block: () -> ObservableSource<Action>) =
    object : Bootstrapper<Action> {
        override fun invoke(): ObservableSource<Action> =
            block()
    }

fun <Action, Effect, State, News> newsPublisher(block: (old: State, action: Action, effect: Effect, new: State) -> News?) =
    object : NewsPublisher<Action, Effect, State, News> {
        override fun invoke(old: State, action: Action, effect: Effect, new: State): News? =
            block(old, action, effect, new)
    }

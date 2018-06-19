package com.badoo.mvicore.android

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.element.TimeCapsule
import com.badoo.mvicore.element.WishToAction
import com.badoo.mvicore.feature.BaseFeature
import com.badoo.mvicore.feature.Feature
import io.reactivex.Observable

interface FeatureFactory {

    fun <Wish : Any, Action : Any, Effect : Any, State : Any, News : Any> create(
        initialState: State,
        bootstrapper: Bootstrapper<Action>? = null,
        wishToAction: WishToAction<Wish, Action>,
        actor: Actor<State, Action, Effect>,
        reducer: Reducer<State, Effect>,
        postProcessor: PostProcessor<Action, Effect, State>? = null,
        newsPublisher: NewsPublisher<Action, Effect, State, News>? = null
    ): Feature<Wish, State, News>
}

class ConcreteFeature1(
    featureFactory: FeatureFactory,
    timeCapsule: TimeCapsule<State>
) : Feature<ConcreteFeature1.Wish, ConcreteFeature1.State, Nothing> by featureFactory.create(
    initialState = timeCapsule[ConcreteFeature1::class] ?: State(),
    wishToAction = { TODO() },
    actor = ActorImpl,
    reducer = ReducerImpl
) {

    init {
        timeCapsule.register(ConcreteFeature1::class, { state.copy(isLoading = false) })
    }

    sealed class Wish

    data class State(val isLoading: Boolean = false) : Parcelable {
        constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte())

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State = State(parcel)

            override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByte(if (isLoading) 1 else 0)
        }

        override fun describeContents(): Int = 0
    }

    private sealed class Action

    private sealed class Effect

    private object ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(p1: State, p2: Action): Observable<out Effect> {
            TODO()
        }
    }

    private object ReducerImpl : Reducer<State, Effect> {
        override fun invoke(p1: State, p2: Effect): State {
            TODO()
        }
    }
}

class ConcreteFeature2(
    timeCapsule: TimeCapsule<State>
) : BaseFeature<ConcreteFeature2.Wish, ConcreteFeature2.Action, ConcreteFeature2.Effect, ConcreteFeature2.State, Nothing>(
    initialState = timeCapsule[STR] ?: State(),
    wishToAction = { TODO() },
    actor = ActorImpl,
    reducer = ReducerImpl
) {

    private companion object {
        val STR: String = ConcreteFeature2::class.java.canonicalName
    }

    init {
        timeCapsule.register(STR, { state.copy(isLoading = false) })
    }

    sealed class Wish

    data class State(val isLoading: Boolean = false) : Parcelable {
        constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte())

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State = State(parcel)

            override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByte(if (isLoading) 1 else 0)
        }

        override fun describeContents(): Int = 0
    }

    sealed class Action

    sealed class Effect

    private object ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(p1: State, p2: Action): Observable<out Effect> {
            TODO()
        }
    }

    private object ReducerImpl : Reducer<State, Effect> {
        override fun invoke(p1: State, p2: Effect): State {
            TODO()
        }
    }
}

fun foo(savedInstanceState: Bundle?) {
    val featureFactory = object : FeatureFactory {
        override fun <Wish : Any, Action : Any, Effect : Any, State : Any, News : Any> create(
            initialState: State,
            bootstrapper: Bootstrapper<Action>?,
            wishToAction: WishToAction<Wish, Action>,
            actor: Actor<State, Action, Effect>,
            reducer: Reducer<State, Effect>,
            postProcessor: PostProcessor<Action, Effect, State>?,
            newsPublisher: NewsPublisher<Action, Effect, State, News>?
        ): Feature<Wish, State, News> {
            TODO()
        }
    }

    // onCreate
    val timeCapsule = AndroidTimeCapsule(savedInstanceState)
    val feature1 = ConcreteFeature1(featureFactory, timeCapsule)
    val feature2 = ConcreteFeature2(timeCapsule)

    fun onSaveInstanceState(outState: Bundle) {
        timeCapsule.saveState(outState)
    }
}

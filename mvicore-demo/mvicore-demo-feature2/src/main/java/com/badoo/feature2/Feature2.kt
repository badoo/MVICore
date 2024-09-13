package com.badoo.feature2

import android.os.Parcelable
import com.badoo.catapi.CatApi
import com.badoo.catapi.Response
import com.badoo.feature2.Feature2.Effect
import com.badoo.feature2.Feature2.Effect.ErrorLoading
import com.badoo.feature2.Feature2.Effect.LoadedImage
import com.badoo.feature2.Feature2.Effect.StartedLoading
import com.badoo.feature2.Feature2.News
import com.badoo.feature2.Feature2.State
import com.badoo.feature2.Feature2.Wish
import com.badoo.feature2.Feature2.Wish.LoadNewImage
import com.badoo.mvicore.android.AndroidMainThreadFeatureScheduler
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.element.TimeCapsule
import com.badoo.mvicore.feature.ActorReducerFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.just
import kotlinx.parcelize.Parcelize

class Feature2(
    timeCapsule: TimeCapsule<Parcelable>? = null
) : ActorReducerFeature<Wish, Effect, State, News>(
    initialState = timeCapsule?.get(Feature2::class.java) ?: State(),
    bootstrapper = BootStrapperImpl(),
    actor = ActorImpl(),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisherImpl(),
    featureScheduler = AndroidMainThreadFeatureScheduler
) {
    init {
        timeCapsule?.register(Feature2::class.java) { state.copy(
            isLoading = false
        )}
    }

    @Parcelize
    data class State(
        val isLoading: Boolean = false,
        val imageUrl: String? = null
    ) : Parcelable

    sealed class Wish {
        data object LoadNewImage : Wish()
    }

    sealed class Effect {
        data object StartedLoading : Effect()
        data class LoadedImage(val url: String) : Effect()
        data class ErrorLoading(val throwable: Throwable) : Effect()
    }

    sealed class News {
        data class ErrorExecutingRequest(val throwable: Throwable) : News()
    }

    class BootStrapperImpl : Bootstrapper<Wish> {
        override fun invoke(): Observable<out Wish> = just(LoadNewImage)
    }

    class ActorImpl : Actor<State, Wish, Effect> {
        private val service = CatApi.service

        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            is LoadNewImage -> loadRandomImage()
                .map { LoadedImage(it.url!!) as Effect }
                .startWith(just(StartedLoading))
                .onErrorReturn { ErrorLoading(it) }
        }

        fun loadRandomImage(): Observable<Response> {
            return service.getRandomImage()
                .randomlyThrowAnException()
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = when (effect) {
            StartedLoading -> state.copy(
                isLoading = true
            )
            is LoadedImage -> state.copy(
                isLoading = false,
                imageUrl = effect.url
            )
            is ErrorLoading -> state.copy(
                isLoading = false
            )
        }
    }

    class NewsPublisherImpl : NewsPublisher<Wish, Effect, State, News> {
        override fun invoke(wish: Wish, effect: Effect, state: State): News? = when (effect) {
            is ErrorLoading -> News.ErrorExecutingRequest(effect.throwable)
            else -> null
        }
    }
}

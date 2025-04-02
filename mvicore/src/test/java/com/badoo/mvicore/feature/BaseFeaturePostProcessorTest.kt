package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.PostProcessorTestFeature.Effect
import com.badoo.mvicore.feature.PostProcessorTestFeature.News
import com.badoo.mvicore.feature.PostProcessorTestFeature.State
import com.badoo.mvicore.feature.PostProcessorTestFeature.Wish
import io.reactivex.rxjava3.core.Observable
import org.junit.jupiter.api.Test

class BaseFeaturePostProcessorTest {

    @Test
    fun `GIVEN feature scheduler provided AND InitialTrigger sent WHEN post processor sends PostProcessorTrigger THEN news is in wish order`() {
        val feature =
            PostProcessorTestFeature(featureScheduler = FeatureSchedulers.TrampolineFeatureScheduler)
        val newsTestObserver = Observable.wrap(feature.news).test()
        feature.accept(Wish.InitialTrigger)

        newsTestObserver.assertValues(News.TriggerNews, News.PostProcessorNews)
    }

    /**
     * The post processor is recursively calling the actor, meaning the news is in reverse order in this scenario.
     */
    @Test
    fun `GIVEN feature scheduler not provided AND InitialTrigger sent WHEN post processor sends PostProcessorTrigger THEN news is in recursive order`() {
        val feature = PostProcessorTestFeature(featureScheduler = null)
        val newsTestObserver = Observable.wrap(feature.news).test()
        feature.accept(Wish.InitialTrigger)

        newsTestObserver.assertValues(News.PostProcessorNews, News.TriggerNews)
    }
}

private class PostProcessorTestFeature(featureScheduler: FeatureScheduler?) :
    BaseFeature<Wish, Wish, Effect, State, News>(
        actor = ActorImpl(),
        initialState = State,
        reducer = ReducerImpl(),
        wishToAction = { it },
        newsPublisher = NewsPublisherImpl(),
        postProcessor = PostProcessorImpl(),
        featureScheduler = featureScheduler
    ) {

    sealed class Wish {
        data object InitialTrigger : Wish()
        data object PostProcessorTrigger : Wish()
    }

    sealed class Effect {
        data object TriggerEffect : Effect()
        data object PostProcessorEffect : Effect()
    }

    object State

    sealed class News {
        data object TriggerNews : News()
        data object PostProcessorNews : News()
    }

    class ActorImpl : Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<out Effect> =
            when (wish) {
                is Wish.InitialTrigger -> Observable.just(Effect.TriggerEffect)
                is Wish.PostProcessorTrigger -> Observable.just(Effect.PostProcessorEffect)
            }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = state
    }

    class NewsPublisherImpl : NewsPublisher<Wish, Effect, State, News> {
        override fun invoke(action: Wish, effect: Effect, state: State): News =
            when (effect) {
                is Effect.TriggerEffect -> News.TriggerNews
                is Effect.PostProcessorEffect -> News.PostProcessorNews
            }
    }

    class PostProcessorImpl : PostProcessor<Wish, Effect, State> {
        override fun invoke(action: Wish, effect: Effect, state: State): Wish? =
            if (action is Wish.InitialTrigger) {
                Wish.PostProcessorTrigger
            } else {
                null
            }
    }
}

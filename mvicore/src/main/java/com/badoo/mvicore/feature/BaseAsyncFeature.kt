package com.badoo.mvicore.feature

import com.badoo.binder.middleware.wrapWithMiddleware
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.element.WishToAction
import com.badoo.mvicore.extension.SameThreadVerifier
import com.badoo.mvicore.extension.asConsumer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.concurrent.atomic.AtomicReference

open class BaseAsyncFeature<Wish : Any, in Action : Any, in Effect : Any, State : Any, News : Any>(
    initialState: State,
    bootstrapper: Bootstrapper<Action>? = null,
    private val wishToAction: WishToAction<Wish, Action>,
    actor: Actor<State, Action, Effect>,
    reducer: Reducer<State, Effect>,
    postProcessor: PostProcessor<Action, Effect, State>? = null,
    newsPublisher: NewsPublisher<Action, Effect, State, News>? = null,
    private val schedulers: AsyncFeatureSchedulers
) : AsyncFeature<Wish, State, News> {

    private val threadVerifier by lazy { SameThreadVerifier(javaClass) }
    private val actionSubject = PublishSubject.create<Action>().toSerialized()
    // store last state to make best effort to return it in getState()
    private val lastState = AtomicReference<State>(initialState)
    private val stateSubject = BehaviorSubject.createDefault(initialState).toSerialized()
    private val newsSubject = PublishSubject.create<News>().toSerialized()
    private val disposables = CompositeDisposable()
    private val postProcessorWrapper = postProcessor?.let {
        PostProcessorWrapper(
            postProcessor,
            actionSubject
        ).wrapWithMiddleware(wrapperOf = postProcessor)
    }

    private val newsPublisherWrapper = newsPublisher?.let {
        NewsPublisherWrapper(
            newsPublisher,
            newsSubject
        ).wrapWithMiddleware(wrapperOf = newsPublisher)
    }

    private val reducerWrapper = ReducerWrapper(
        reducer,
        stateSubject,
        postProcessorWrapper,
        newsPublisherWrapper
    ).wrapWithMiddleware(wrapperOf = reducer)

    private val actorWrapper = ActorWrapper(
        disposables,
        actor,
        stateSubject,
        reducerWrapper,
        schedulers.featureScheduler,
        lazy { threadVerifier } // pass as lazy to not initialize here
    ).wrapWithMiddleware(wrapperOf = actor)

    init {
        disposables += stateSubject.subscribe { lastState.set(it) }
        disposables += actorWrapper
        disposables += reducerWrapper
        disposables += postProcessorWrapper
        disposables += newsPublisherWrapper
        disposables +=
            actionSubject
                .observeOn(schedulers.featureScheduler)
                .subscribe { invokeActor(state, it) }

        if (bootstrapper != null) {
            actionSubject
                .asConsumer()
                .wrapWithMiddleware(
                    wrapperOf = bootstrapper,
                    postfix = "output"
                ).also { output ->
                    disposables += output
                    disposables +=
                        Observable
                            .defer { bootstrapper() }
                            .subscribeOn(schedulers.featureScheduler)
                            .observeOn(schedulers.featureScheduler)
                            .subscribe { output.accept(it) }
                }
        }
    }

    override val backgroundStates: Observable<State>
        get() = stateSubject

    override val backgroundNews: Observable<News>
        get() = newsSubject

    override val state: State
        get() = lastState.get()

    override val news: ObservableSource<News>
        get() = newsSubject.observeOn(schedulers.observationScheduler)

    override fun subscribe(observer: Observer<in State>) {
        stateSubject
            .observeOn(schedulers.observationScheduler)
            .subscribe(observer)
    }

    override fun accept(wish: Wish) {
        val action = wishToAction.invoke(wish)
        actionSubject.onNext(action)
    }

    override fun dispose() {
        disposables.dispose()
    }

    override fun isDisposed(): Boolean =
        disposables.isDisposed

    private fun invokeActor(state: State, action: Action) {
        if (isDisposed) return

        if (actorWrapper is ActorWrapper<State, Action, *>) {
            // there's no middleware around it, so we can optimise here by not creating any extra objects
            actorWrapper.processAction(state, action)

        } else {
            // there are middlewares around it, and we must treat it as Consumer
            actorWrapper.accept(Pair(state, action))
        }
    }

    private operator fun CompositeDisposable.plusAssign(any: Any?) {
        if (any is Disposable) add(any)
    }

    private class ActorWrapper<State : Any, Action : Any, Effect : Any>(
        private val disposables: CompositeDisposable,
        private val actor: Actor<State, Action, Effect>,
        private val stateSubject: Subject<State>,
        private val reducerWrapper: Consumer<Triple<State, Action, Effect>>,
        private val featureScheduler: Scheduler,
        private val threadVerifier: Lazy<SameThreadVerifier>
    ) : Consumer<Pair<State, Action>> {

        // record-playback entry point
        override fun accept(t: Pair<State, Action>) {
            val (state, action) = t
            processAction(state, action)
        }

        fun processAction(state: State, action: Action) {
            if (disposables.isDisposed) return

            var disposable: Disposable? = null
            disposable =
                actor
                    .invoke(state, action)
                    .observeOn(featureScheduler)
                    .doAfterTerminate {
                        // Remove disposables manually because CompositeDisposable does not do it automatically producing memory leaks
                        // Check for null as it might be disposed instantly
                        disposable?.let(disposables::remove)
                    }
                    .subscribe { effect -> invokeReducer(action, effect) }
            // Disposable might be already disposed in case of no scheduler + Observable.just
            if (!disposable.isDisposed) disposables += disposable
        }

        private fun invokeReducer(action: Action, effect: Effect) {
            if (disposables.isDisposed) return
            val state =
                if (stateSubject is BehaviorSubject<State>) {
                    requireNotNull(stateSubject.value)
                } else {
                    // if serialized wait for async processes to complete and get the actual value
                    // blockingFirst is happening on the featureScheduler in this case
                    stateSubject.blockingFirst()
                }

            threadVerifier.value.verify()
            if (reducerWrapper is ReducerWrapper) {
                // there's no middleware around it, so we can optimise here by not creating any extra objects
                reducerWrapper.processEffect(state, action, effect)

            } else {
                // there are middlewares around it, and we must treat it as Consumer
                reducerWrapper.accept(Triple(state, action, effect))
            }
        }
    }

    private class ReducerWrapper<State : Any, Action : Any, Effect : Any>(
        private val reducer: Reducer<State, Effect>,
        private val states: Subject<State>,
        private val postProcessorWrapper: Consumer<Triple<Action, Effect, State>>?,
        private val newsPublisherWrapper: Consumer<Triple<Action, Effect, State>>?
    ) : Consumer<Triple<State, Action, Effect>> {

        // record-playback entry point
        override fun accept(t: Triple<State, Action, Effect>) {
            val (state, action, effect) = t
            processEffect(state, action, effect)
        }

        fun processEffect(state: State, action: Action, effect: Effect) {
            val newState = reducer.invoke(state, effect)
            states.onNext(newState)
            invokePostProcessor(action, effect, newState)
            invokeNewsPublisher(action, effect, newState)
        }

        private fun invokePostProcessor(action: Action, effect: Effect, state: State) {
            postProcessorWrapper?.let {
                if (postProcessorWrapper is PostProcessorWrapper) {
                    // there's no middleware around it, so we can optimise here by not creating any extra objects
                    postProcessorWrapper.postProcess(action, effect, state)

                } else {
                    // there are middlewares around it, and we must treat it as Consumer
                    postProcessorWrapper.accept(Triple(action, effect, state))
                }
            }
        }

        private fun invokeNewsPublisher(action: Action, effect: Effect, state: State) {
            newsPublisherWrapper?.let {
                if (newsPublisherWrapper is NewsPublisherWrapper<Action, Effect, State, *>) {
                    // there's no middleware around it, so we can optimise here by not creating any extra objects
                    newsPublisherWrapper.publishNews(action, effect, state)

                } else {
                    // there are middlewares around it, and we must treat it as Consumer
                    newsPublisherWrapper.accept(Triple(action, effect, state))
                }
            }
        }
    }

    private class PostProcessorWrapper<Action : Any, Effect : Any, State : Any>(
        private val postProcessor: PostProcessor<Action, Effect, State>,
        private val actions: Subject<Action>
    ) : Consumer<Triple<Action, Effect, State>> {

        // record-playback entry point
        override fun accept(t: Triple<Action, Effect, State>) {
            val (action, effect, state) = t
            postProcess(action, effect, state)
        }

        fun postProcess(action: Action, effect: Effect, state: State) {
            postProcessor.invoke(action, effect, state)?.let {
                actions.onNext(it)
            }
        }
    }

    private class NewsPublisherWrapper<Action : Any, Effect : Any, State : Any, News : Any>(
        private val newsPublisher: NewsPublisher<Action, Effect, State, News>,
        private val news: Subject<News>
    ) : Consumer<Triple<Action, Effect, State>> {

        // record-playback entry point
        override fun accept(t: Triple<Action, Effect, State>) {
            val (action, effect, state) = t
            publishNews(action, effect, state)
        }

        fun publishNews(action: Action, effect: Effect, state: State) {
            newsPublisher.invoke(action, effect, state)?.let {
                news.onNext(it)
            }
        }
    }
}

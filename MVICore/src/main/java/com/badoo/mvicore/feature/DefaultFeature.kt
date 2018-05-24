package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.element.WishToAction
import com.badoo.mvicore.extension.assertOnMainThread
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

open class DefaultFeature<Wish : Any, in Action : Any, in Effect : Any, State : Any>(
    initialState: State,
    bootstrapper: Bootstrapper<Action>? = null,
    private val wishToAction: WishToAction<Wish, Action>,
    private val actor: Actor<State, Action, Effect>,
    private val reducer: Reducer<State, Effect>,
    private val postProcessor: PostProcessor<Action, Effect, State>? = null
) : Feature<Wish, State> {

    private val actionSubject = PublishSubject.create<Action>()
    private val stateSubject = BehaviorSubject.createDefault(initialState)
    private val newsSubject: Subject<News> = PublishSubject.create()
    private val disposables = CompositeDisposable()

    init {
        bootstrapper?.let {
            disposables += it.invoke().subscribe {
                actionSubject.onNext(it)
            }
        }

        disposables += actionSubject
            .flatMap { action ->
                actor.invoke(state, action)
                    .doOnNext { effect ->
                        assertOnMainThread()
                        val newState = reducer.invoke(state, effect)
                        stateSubject.onNext(newState)
                        postProcessor?.let {
                            it.invoke(action, effect, newState)?.let {
                                actionSubject.onNext(it)
                            }
                        }
                    }
            }
            .subscribe {
                if (it is News) {
                    newsSubject.onNext(it)
                }
            }
    }

    override val state: State
        get() {
            assertOnMainThread()
            return stateSubject.value!!
        }

    override val news: ObservableSource<News>
        get() = newsSubject


    override fun subscribe(observer: Observer<in State>) {
        stateSubject.subscribe(observer)
    }

    override fun accept(wish: Wish) {
        assertOnMainThread()
        val action = wishToAction.invoke(wish)
        actionSubject.onNext(action)
    }

    override fun dispose() {
        disposables.dispose()
    }

    override fun isDisposed(): Boolean =
        disposables.isDisposed
}

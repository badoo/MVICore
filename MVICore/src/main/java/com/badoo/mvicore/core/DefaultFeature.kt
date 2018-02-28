package com.badoo.mvicore.core

import com.badoo.mvicore.assertOnMainThread
import com.badoo.mvicore.element.News
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Default implementation of [Feature] with the help of a [Configuration].
 * Must be created on the main thread.
 */
class DefaultFeature<State : Any, in Wish : Any, Effect : Any>(
        configuration: Configuration<State, Wish, Effect>
) : Feature<State, Wish> {
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val wishes = PublishSubject.create<Wish>()
    private var reducer = configuration.reducer
    private val stateSubject = BehaviorSubject.createDefault(configuration.initialState)


    init {
        configuration.middlewares?.forEach { reducer = it.create(reducer) }

        disposables += wishes
                .flatMap {
                    configuration.actor.invoke(it, state)
                        .doOnNext {
                            assertOnMainThread()
                            stateSubject.onNext(configuration.reducer(state, it))
                        }
                }
                .subscribe {
                    if (it is News) {
                        configuration.newsPublisher?.invoke(it)
                    }
                }

        configuration.bootstrapper?.invoke()
                ?.subscribe { onWish(it) }
                ?.let { disposables += it }
    }

    override fun onWish(wish: Wish) {
        assertOnMainThread()
        wishes.onNext(wish)
    }

    override val state: State
        get() = stateSubject.value

    override val states: Observable<State> =
        stateSubject

    override fun dispose() {
        disposables.dispose()
    }

    override fun isDisposed(): Boolean =
        disposables.isDisposed
}

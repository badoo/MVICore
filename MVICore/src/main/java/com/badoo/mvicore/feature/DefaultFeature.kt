package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.extension.assertOnMainThread
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class DefaultFeature<Wish : Any, State : Any, Effect : Any>(
    initialState: State,
    private val actor: Actor<Wish, State, Effect>,
    private val reducer: Reducer<State, Effect>
) : Feature<Wish, State> {
    private val wishSubject = PublishSubject.create<Wish>()
    private val stateSubject = BehaviorSubject.createDefault(initialState)
    private val newsSubject: Subject<News> = PublishSubject.create()
    private val disposable: Disposable

    init {
        disposable = wishSubject
            .flatMap {
                actor.invoke(it, state)
                    .doOnNext {
                        assertOnMainThread()
                        stateSubject.onNext(reducer.invoke(state, it))
                    }
            }
            .subscribe {
                if (it is News) {
                    newsSubject.onNext(it)
                }
            }
    }

    override val state: State
        get() = stateSubject.value

    override val news: ObservableSource<News>
        get() = newsSubject


    override fun accept(wish: Wish) {
        wishSubject.onNext(wish)
    }

    override fun subscribe(observer: Observer<in State>) {
        stateSubject.subscribe(observer)
    }

    override fun dispose() {
        disposable.dispose()
    }

    override fun isDisposed(): Boolean =
        disposable.isDisposed
}

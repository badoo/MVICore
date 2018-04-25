package com.badoo.mvicore.feature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.News
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.extension.assertOnMainThread
import com.badoo.mvicore.feature.internal.Disposables
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class DefaultFeature<Wish : Any, State : Any, Effect : Any>(
    initialState: State,
    private val actor: Actor<Wish, State, Effect>,
    private val reducer: Reducer<State, Effect>
) : Feature<Wish, State> {
    private val stateSubject = BehaviorSubject.createDefault(initialState)
    private val newsSubject: Subject<News> = PublishSubject.create()
    private val disposables = Disposables()
    private val processEffect: (Effect) -> State = ::processEffect

    override val state: State
        get() = stateSubject.value

    override val news: ObservableSource<News>
        get() = newsSubject


    override fun accept(wish: Wish) {
        if (!isDisposed) {
            actor
                .invoke(wish, state, processEffect)
                ?.also(disposables::add)
        }
    }

    override fun subscribe(observer: Observer<in State>) {
        stateSubject.subscribe(observer)
    }

    override fun dispose() {
        disposables.dispose()
    }

    override fun isDisposed(): Boolean =
        disposables.isDisposed

    private fun processEffect(effect: Effect): State {
        assertOnMainThread()

        return reducer
            .invoke(state, effect)
            .also {
                stateSubject.onNext(it)
                if (effect is News) {
                    newsSubject.onNext(effect)
                }
            }
    }
}

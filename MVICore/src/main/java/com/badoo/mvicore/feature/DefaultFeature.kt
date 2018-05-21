package com.badoo.mvicore.feature

import com.badoo.mvicore.element.News
import com.badoo.mvicore.extension.assertOnMainThread
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * 1. Wish is the public API
 * 2. NEW: Action is the "internal Wish", a superset of public Wishes.
 *         This way you don't have to expose InvalidateCache as public Wish
 * 3. NEW: Wish -> Action mapped by WishMapper
 * 3. Actor reacts to Actions instead of Wishes (and can remain simple interface)
 * 4. Reducer is the same
 * 5. News is the same
 * 6. NEW: PostProcessor, which enables you to react on
 *         whatever (wish -> effect -> state) combination
 *         to fire off additional actions
 *
 * See implementations in example package.
 *
 * Equivalent to dispatch() approach in:
 *      I. You have all the information you need in PostProcessor to do any kind of additional stuff
 *      II. The async part of reacting to it will still be written inside Actor
 *      III. All your business logic is still inside your Feature, and internals are not leaked to public API
 *
 * Different to dispatch() approach:
 *      I. Deciding on additional actions is centralised to PostProcessor.
 *         This also serves as a separation of concerns: Actor's only responsibility is now to
 *         execute async Actions but not to decide when to do them
 *
 * The BIG difference:
 *      I. CONS of the dispatch() approach:
 *         If you decide to do additional actions after checking the result of dispatch(),
 *         you will react on the state returned to you. This state is NOT GUARANTEED TO BE THE SAME
 *         the next time you call Actor, since this is NOT the state you have in Actor, but the
 *         new state held by feature, modified by the emitted effect.
 *         So practically if you are in timetravel / playback mode, replaying to your Actor,
 *         dispatch will return to you the feature is in at the time of playback, and not at the
 *         time of recording, and you will not be able to see the same execution path!
 *
 *      II. PROS of having Actions + PostProcessor:
 *         Both your Actor and your PostProcessor can be wrapped with middleware.
 *         This means:
 *              - Actor on replay will see BOTH Actions from Wishes AND Actions triggered by
 *                PostProcessor, even if feature is now in a different state
 *              - PostProcessor can also be replayed to check why it triggered certain Actions.
 *                This information is NOT available with the dispatch() approach.
 *              - This means a LOT more debugging power
 *              - Even if you don't have time travel, just logging, it will still give you more
 *                information why things happened
 *
 *  Also see example package SimpleFeature + SimpleFeatureAdapter classes to see how
 *  nothing changes in complexity for simple implementations
 */
open class DefaultFeature<Wish : Any, Action : Any, Effect : Any, State : Any>(
    initialState: State,
    private val wishMapper: WishMapper<Wish, Action>,
    private val actor: Actor<State, Action, Effect>,
    private val reducer: Reducer<State, Effect>,
    private val postProcessor: PostProcessor<Action, Effect, State>? = null
) : Feature<Wish, State> {

    interface WishMapper<in Wish : Any, Action : Any> {
        operator fun invoke(wish: Wish): Action
    }

    interface Actor<State : Any, in Action : Any, Effect : Any> {
        operator fun invoke(state: State, action: Action): Observable<Effect>
    }

    interface Reducer<State : Any, in Effect : Any> {
        operator fun invoke(state: State, effect: Effect): State
    }

    interface PostProcessor<Action : Any, Effect : Any, State : Any> {
        operator fun invoke(action: Action, effect: Effect, state: State): Action?
    }

    private val actionSubject = PublishSubject.create<Action>()
    private val stateSubject = BehaviorSubject.createDefault(initialState)
    private val newsSubject: Subject<News> = PublishSubject.create()
    private val disposable: Disposable

    init {
        disposable = actionSubject
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
        val action = wishMapper.invoke(wish)
        actionSubject.onNext(action)
    }

    override fun dispose() {
        disposable.dispose()
    }

    override fun isDisposed(): Boolean =
        disposable.isDisposed
}

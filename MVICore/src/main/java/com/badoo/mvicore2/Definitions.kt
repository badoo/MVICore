package com.badoo.mvicore2

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer

interface Store<State : Any, Wish : Any> : Observer<Wish>, ObservableSource<State>

interface Actor<in Wish, State> : (Wish, State) -> Observable<Effect<State>>

interface Effect<State> : (State) -> State

interface ViewBinder<Event : Any, ViewModel : Any> : Observer<ViewModel>, ObservableSource<Event>

interface Transformer<in From, out To> : (From) -> To

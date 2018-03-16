package com.badoo.mvicore.boundary

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Gluing layer between UI and Feature
 */
interface Boundary<in UiEvent : Any, ViewModel : Any> : Disposable {

    fun onUiEvent(uiEvent: UiEvent)

    val viewModels: Observable<ViewModel>
}

package com.badoo.mvicore.feature.internal

import io.reactivex.disposables.Disposable

internal class DisposableCollection : Disposable {

    private var disposables: MutableList<Disposable>? = mutableListOf()

    override fun isDisposed(): Boolean =
        disposables == null

    override fun dispose() {
        disposables?.forEach { it.dispose() }
        disposables = null
    }

    fun add(disposable: Disposable) {
        disposables?.apply {
            removeAll { it.isDisposed }
            add(disposable)
        }
    }

    operator fun plusAssign(disposable: Disposable) {
        add(disposable)
    }
}

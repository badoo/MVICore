package com.badoo.mvicoredemo.di.usersessionscope.component

import com.badoo.mvicoredemo.di.ScopedComponent
import com.badoo.mvicoredemo.App
import com.badoo.mvicoredemo.di.usersessionscope.module.FeatureModule
import io.reactivex.disposables.Disposable

object UserSessionScopedComponent : ScopedComponent<UserSessionComponent>() {

    override fun create(): UserSessionComponent =
        DaggerUserSessionComponent
            .builder()
            .appComponent(App.component.dependAndGet(this))
            .featureModule(FeatureModule())
            .build()

    override fun UserSessionComponent.disposables(): Array<Disposable> = arrayOf(
        feature1(),
        feature2()
    )
}

package com.badoo.mvicoredemo.di.usersessionscope;

import android.app.Application
import com.badoo.mvicoredemo.di.usersessionscope.component.UserComponentEntryPoint
import com.badoo.mvicoredemo.di.usersessionscope.component.UserSessionComponent
import dagger.hilt.EntryPoints
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val userSessionComponentBuilderProvider: Provider<UserSessionComponent.Builder>
) {
    var userComponent: UserSessionComponent? = null
        private set

    fun userLoggedIn() {
        userComponent = userSessionComponentBuilderProvider.get().build()
    }

    fun logout() {
        userComponent?.apply {
            val userPartsEntryPoint = UserComponentEntryPoint.get(this)
            CompositeDisposable()
                .apply {
                    addAll(
                        userPartsEntryPoint.feature1(),
                        userPartsEntryPoint.feature2(),
                    )
                }
                .dispose()
        }
        userComponent = null
    }

    companion object {
        fun getUserManager(application: Application): UserManager =
            EntryPoints
                .get(application, UserManagerEntryPoint::class.java)
                .userManager()
    }
}

package com.badoo.mvicoredemo.di.usersessionscope.component

import dagger.hilt.DefineComponent
import dagger.hilt.components.SingletonComponent

@UserSessionScope
@DefineComponent(parent = SingletonComponent::class)
interface UserSessionComponent {
    @DefineComponent.Builder
    interface Builder {
        fun build(): UserSessionComponent
    }
}

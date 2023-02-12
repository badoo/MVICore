package com.badoo.mvicoredemo.di.usersessionscope

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface UserManagerEntryPoint {
  fun userManager(): UserManager
}

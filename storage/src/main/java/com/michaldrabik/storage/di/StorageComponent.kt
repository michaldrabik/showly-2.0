package com.michaldrabik.storage.di

import com.michaldrabik.storage.UserManager
import com.michaldrabik.storage.database.AppDatabase
import dagger.Component

@Component(modules = [StorageModule::class])
interface StorageComponent {

  fun userRepository(): UserManager

  fun database(): AppDatabase
}
package com.michaldrabik.storage.di

import com.michaldrabik.storage.database.AppDatabase
import dagger.Component

@Component(modules = [StorageModule::class])
interface StorageComponent {

  fun database(): AppDatabase
}
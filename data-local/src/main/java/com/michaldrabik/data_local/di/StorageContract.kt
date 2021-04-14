package com.michaldrabik.data_local.di

import com.michaldrabik.data_local.database.AppDatabase

interface StorageContract {
  fun database(): AppDatabase
}

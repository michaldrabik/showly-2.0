package com.michaldrabik.storage.di

import com.michaldrabik.storage.database.AppDatabase

interface StorageMarker {
  fun database(): AppDatabase
}
package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Settings

interface SettingsLocalDataSource {

  suspend fun getAll(): Settings

  suspend fun getCount(): Int

  suspend fun upsert(settings: Settings)
}

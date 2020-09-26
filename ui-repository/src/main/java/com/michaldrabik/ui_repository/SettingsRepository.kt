package com.michaldrabik.ui_repository

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class SettingsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun isInitialized() =
    database.settingsDao().getCount() > 0

  suspend fun load(): Settings {
    val settingsDb = database.settingsDao().getAll()
    return settingsDb.let { mappers.settings.fromDatabase(it) }
  }

  suspend fun update(settings: Settings) {
    database.withTransaction {
      val settingsDb = mappers.settings.toDatabase(settings)
      database.settingsDao().upsert(settingsDb)
    }
  }
}

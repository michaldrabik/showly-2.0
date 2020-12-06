package com.michaldrabik.ui_repository

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject
import javax.inject.Named

@AppScope
class SettingsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  @Named("miscPreferences") private var miscPreferences: SharedPreferences
) {

  companion object {
    private const val KEY_LANGUAGE = "KEY_LANGUAGE"
  }

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

  suspend fun isMoviesEnabled() = isInitialized() && load().moviesEnabled

  fun getLanguage() = miscPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

  fun setLanguage(language: String) = miscPreferences.edit().putString(KEY_LANGUAGE, language).apply()

  suspend fun clearLanguageLogs() = database.translationsSyncLogDao().deleteAll()
}

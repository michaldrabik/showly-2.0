package com.michaldrabik.ui_repository

import android.app.UiModeManager.MODE_NIGHT_NO
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.withTransaction
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.Mode
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject
import javax.inject.Named

@AppScope
class SettingsRepository @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  companion object {
    const val KEY_LANGUAGE = "KEY_LANGUAGE"
    private const val KEY_MOVIES_ENABLED = "KEY_MOVIES_ENABLED"
    private const val KEY_MODE = "KEY_MOVIES_MODE"
    private const val KEY_THEME = "KEY_THEME"
  }

  suspend fun isInitialized() =
    database.settingsDao().getCount() > 0

  suspend fun load(): Settings {
    val settingsDb = database.settingsDao().getAll()
    return mappers.settings.fromDatabase(settingsDb)
  }

  suspend fun update(settings: Settings) {
    database.withTransaction {
      val settingsDb = mappers.settings.toDatabase(settings)
      database.settingsDao().upsert(settingsDb)
    }
  }

  fun isMoviesEnabled() =
    miscPreferences.getBoolean(KEY_MOVIES_ENABLED, true)

  fun setMoviesEnabled(enabled: Boolean) =
    miscPreferences.edit().putBoolean(KEY_MOVIES_ENABLED, enabled).apply()

  fun getMode(): Mode {
    val default = Mode.SHOWS.name
    return Mode.valueOf(miscPreferences.getString(KEY_MODE, default) ?: default)
  }

  fun setMode(mode: Mode) = miscPreferences.edit().putString(KEY_MODE, mode.name).apply()

  fun getLanguage() = miscPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

  fun setLanguage(language: String) = miscPreferences.edit().putString(KEY_LANGUAGE, language).apply()

  fun setTheme(theme: Int) = miscPreferences.edit { putInt(KEY_THEME, theme) }

  fun getTheme() = miscPreferences.getInt(KEY_THEME, MODE_NIGHT_NO)

  suspend fun clearLanguageLogs() {
    database.withTransaction {
      database.translationsSyncLogDao().deleteAll()
      database.translationsMoviesSyncLogDao().deleteAll()
    }
  }

  suspend fun clearUnusedTranslations(input: List<String>) {
    database.withTransaction {
      database.showTranslationsDao().deleteByLanguage(input)
      database.movieTranslationsDao().deleteByLanguage(input)
      database.episodeTranslationsDao().deleteByLanguage(input)
    }
  }
}

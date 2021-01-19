package com.michaldrabik.ui_repository

import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.withTransaction
import com.michaldrabik.common.Config.DEFAULT_COUNTRY
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
    private const val KEY_COUNTRY = "KEY_COUNTRY"
    private const val KEY_MOVIES_ENABLED = "KEY_MOVIES_ENABLED"
    private const val KEY_MODE = "KEY_MOVIES_MODE"
    private const val KEY_THEME = "KEY_THEME"
    private const val KEY_THEME_WIDGET = "KEY_THEME_WIDGET"
    private const val KEY_THEME_WIDGET_TRANSPARENT = "KEY_THEME_WIDGET_TRANSPARENT"
    private const val KEY_PREMIUM = "KEY_THEME_WIDGET"
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

  var mode: Mode
    get() {
      val default = Mode.SHOWS.name
      return Mode.valueOf(miscPreferences.getString(KEY_MODE, default) ?: default)
    }
    set(value) = miscPreferences.edit(true) { putString(KEY_MODE, value.name) }

  var isPremium: Boolean
    get() = miscPreferences.getBoolean(KEY_PREMIUM, false)
    set(value) = miscPreferences.edit(true) { putBoolean(KEY_PREMIUM, value) }

  var isMoviesEnabled: Boolean
    get() = miscPreferences.getBoolean(KEY_MOVIES_ENABLED, true)
    set(value) = miscPreferences.edit(true) { putBoolean(KEY_MOVIES_ENABLED, value) }

  var language: String
    get() = miscPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    set(value) = miscPreferences.edit(true) { putString(KEY_LANGUAGE, value) }

  var country: String
    get() = miscPreferences.getString(KEY_COUNTRY, DEFAULT_COUNTRY) ?: DEFAULT_COUNTRY
    set(value) = miscPreferences.edit(true) { putString(KEY_COUNTRY, value) }

  var theme: Int
    get() = miscPreferences.getInt(KEY_THEME, MODE_NIGHT_YES)
    set(value) = miscPreferences.edit(true) { putInt(KEY_THEME, value) }

  var widgetsTheme: Int
    get() = miscPreferences.getInt(KEY_THEME_WIDGET, MODE_NIGHT_YES)
    set(value) = miscPreferences.edit(true) { putInt(KEY_THEME_WIDGET, value) }

  var widgetsTransparency: Int
    get() = miscPreferences.getInt(KEY_THEME_WIDGET_TRANSPARENT, 100)
    set(value) = miscPreferences.edit(true) { putInt(KEY_THEME_WIDGET_TRANSPARENT, value) }

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

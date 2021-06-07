package com.michaldrabik.repository

import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.withTransaction
import com.michaldrabik.common.Config.DEFAULT_COUNTRY
import com.michaldrabik.common.Config.DEFAULT_DATE_FORMAT
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.ProgressType
import com.michaldrabik.ui_model.Settings
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
  @Named("miscPreferences") private var miscPreferences: SharedPreferences,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  companion object {
    const val KEY_LANGUAGE = "KEY_LANGUAGE"
    private const val KEY_COUNTRY = "KEY_COUNTRY"
    private const val KEY_MOVIES_ENABLED = "KEY_MOVIES_ENABLED"
    private const val KEY_NEWS_ENABLED = "KEY_NEWS_ENABLED"
    private const val KEY_MODE = "KEY_MOVIES_MODE"
    private const val KEY_THEME = "KEY_THEME"
    private const val KEY_THEME_WIDGET = "KEY_THEME_WIDGET"
    private const val KEY_THEME_WIDGET_TRANSPARENT = "KEY_THEME_WIDGET_TRANSPARENT"
    private const val KEY_PREMIUM = "KEY_PREMIUM"
    private const val KEY_DATE_FORMAT = "KEY_DATE_FORMAT"
    private const val KEY_PROGRESS_PERCENT = "KEY_PROGRESS_PERCENT"
    private const val KEY_USER_ID = "KEY_USER_ID"
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

  var isNewsEnabled: Boolean
    get() = miscPreferences.getBoolean(KEY_NEWS_ENABLED, false)
    set(value) = miscPreferences.edit(true) { putBoolean(KEY_NEWS_ENABLED, value) }

  var language: String
    get() = miscPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    set(value) = miscPreferences.edit(true) { putString(KEY_LANGUAGE, value) }

  var country: String
    get() = miscPreferences.getString(KEY_COUNTRY, DEFAULT_COUNTRY) ?: DEFAULT_COUNTRY
    set(value) = miscPreferences.edit(true) { putString(KEY_COUNTRY, value) }

  var theme: Int
    get() {
      if (!isPremium) return MODE_NIGHT_YES
      return miscPreferences.getInt(KEY_THEME, MODE_NIGHT_YES)
    }
    set(value) = miscPreferences.edit(true) { putInt(KEY_THEME, value) }

  var widgetsTheme: Int
    get() {
      if (!isPremium) return MODE_NIGHT_YES
      return miscPreferences.getInt(KEY_THEME_WIDGET, MODE_NIGHT_YES)
    }
    set(value) = miscPreferences.edit(true) { putInt(KEY_THEME_WIDGET, value) }

  var widgetsTransparency: Int
    get() {
      if (!isPremium) return 100
      return miscPreferences.getInt(KEY_THEME_WIDGET_TRANSPARENT, 100)
    }
    set(value) = miscPreferences.edit(true) { putInt(KEY_THEME_WIDGET_TRANSPARENT, value) }

  var dateFormat: String
    get() = miscPreferences.getString(KEY_DATE_FORMAT, DEFAULT_DATE_FORMAT) ?: DEFAULT_DATE_FORMAT
    set(value) = miscPreferences.edit(true) { putString(KEY_DATE_FORMAT, value) }

  var progressPercentType: ProgressType
    get() {
      val setting = miscPreferences.getString(KEY_PROGRESS_PERCENT, ProgressType.AIRED.name) ?: ProgressType.AIRED.name
      return ProgressType.valueOf(setting)
    }
    set(value) = miscPreferences.edit(true) { putString(KEY_PROGRESS_PERCENT, value.name) }

  val userId
    get() = when (val id = miscPreferences.getString(KEY_USER_ID, null)) {
      null -> {
        val uuid = UUID.randomUUID().toString().take(13)
        miscPreferences.edit().putString(KEY_USER_ID, uuid).apply()
        uuid
      }
      else -> id
    }

  suspend fun revokePremium() {
    val settings = load()
    update(settings.copy(traktQuickRateEnabled = false))
    isPremium = false
    theme = MODE_NIGHT_YES
    widgetsTheme = MODE_NIGHT_YES
    widgetsTransparency = 100
    isNewsEnabled = false
  }

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

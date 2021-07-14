package com.michaldrabik.repository

import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.withTransaction
import com.michaldrabik.common.Config.DEFAULT_COUNTRY
import com.michaldrabik.common.Config.DEFAULT_DATE_FORMAT
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.Mode
import com.michaldrabik.common.delegates.BooleanPreference
import com.michaldrabik.common.delegates.StringPreference
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

  companion object Key {
    const val LANGUAGE = "KEY_LANGUAGE"
    private const val COUNTRY = "KEY_COUNTRY"
    private const val DATE_FORMAT = "KEY_DATE_FORMAT"
    private const val MODE = "KEY_MOVIES_MODE"
    private const val MOVIES_ENABLED = "KEY_MOVIES_ENABLED"
    private const val NEWS_ENABLED = "KEY_NEWS_ENABLED"
    private const val PREMIUM = "KEY_PREMIUM"
    private const val PROGRESS_PERCENT = "KEY_PROGRESS_PERCENT"
    private const val STREAMINGS_ENABLED = "KEY_STREAMINGS_ENABLED"
    private const val THEME = "KEY_THEME"
    private const val THEME_WIDGET = "KEY_THEME_WIDGET"
    private const val THEME_WIDGET_TRANSPARENT = "KEY_THEME_WIDGET_TRANSPARENT"
    private const val USER_ID = "KEY_USER_ID"
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
      return Mode.valueOf(miscPreferences.getString(MODE, default) ?: default)
    }
    set(value) = miscPreferences.edit(true) { putString(MODE, value.name) }

  var isPremium by BooleanPreference(miscPreferences, PREMIUM)
  var streamingsEnabled by BooleanPreference(miscPreferences, STREAMINGS_ENABLED, true)
  var isMoviesEnabled by BooleanPreference(miscPreferences, MOVIES_ENABLED, true)
  var isNewsEnabled by BooleanPreference(miscPreferences, NEWS_ENABLED)
  var language by StringPreference(miscPreferences, LANGUAGE, DEFAULT_LANGUAGE)
  var country by StringPreference(miscPreferences, COUNTRY, DEFAULT_COUNTRY)
  var dateFormat by StringPreference(miscPreferences, DATE_FORMAT, DEFAULT_DATE_FORMAT)

  var theme: Int
    get() {
      if (!isPremium) return MODE_NIGHT_YES
      return miscPreferences.getInt(THEME, MODE_NIGHT_YES)
    }
    set(value) = miscPreferences.edit(true) { putInt(THEME, value) }

  var widgetsTheme: Int
    get() {
      if (!isPremium) return MODE_NIGHT_YES
      return miscPreferences.getInt(THEME_WIDGET, MODE_NIGHT_YES)
    }
    set(value) = miscPreferences.edit(true) { putInt(THEME_WIDGET, value) }

  var widgetsTransparency: Int
    get() {
      if (!isPremium) return 100
      return miscPreferences.getInt(THEME_WIDGET_TRANSPARENT, 100)
    }
    set(value) = miscPreferences.edit(true) { putInt(THEME_WIDGET_TRANSPARENT, value) }

  var progressPercentType: ProgressType
    get() {
      val setting = miscPreferences.getString(PROGRESS_PERCENT, ProgressType.AIRED.name) ?: ProgressType.AIRED.name
      return ProgressType.valueOf(setting)
    }
    set(value) = miscPreferences.edit(true) { putString(PROGRESS_PERCENT, value.name) }

  val userId
    get() = when (val id = miscPreferences.getString(USER_ID, null)) {
      null -> {
        val uuid = UUID.randomUUID().toString().take(13)
        miscPreferences.edit().putString(USER_ID, uuid).apply()
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
    with(database) {
      withTransaction {
        translationsSyncLogDao().deleteAll()
        translationsMoviesSyncLogDao().deleteAll()
      }
    }
  }

  suspend fun clearUnusedTranslations(input: List<String>) {
    with(database) {
      withTransaction {
        showTranslationsDao().deleteByLanguage(input)
        movieTranslationsDao().deleteByLanguage(input)
        episodeTranslationsDao().deleteByLanguage(input)
      }
    }
  }
}

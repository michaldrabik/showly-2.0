package com.michaldrabik.repository.settings

import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.SharedPreferences
import androidx.core.content.edit
import com.michaldrabik.common.Config.DEFAULT_COUNTRY
import com.michaldrabik.common.Config.DEFAULT_DATE_FORMAT
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.Config.DEFAULT_NEWS_VIEW_TYPE
import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.utilities.BooleanPreference
import com.michaldrabik.repository.utilities.EnumPreference
import com.michaldrabik.repository.utilities.LongPreference
import com.michaldrabik.repository.utilities.StringPreference
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.ProgressNextEpisodeType.LAST_WATCHED
import com.michaldrabik.ui_model.ProgressType
import com.michaldrabik.ui_model.Settings
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
  val sorting: SettingsSortRepository,
  val filters: SettingsFiltersRepository,
  val widgets: SettingsWidgetsRepository,
  val viewMode: SettingsViewModeRepository,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  @Named("miscPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    const val LANGUAGE = "KEY_LANGUAGE"
    internal const val PREMIUM = "KEY_PREMIUM"
    private const val COUNTRY = "KEY_COUNTRY"
    private const val DATE_FORMAT = "KEY_DATE_FORMAT"
    private const val MODE = "KEY_MOVIES_MODE"
    private const val MOVIES_ENABLED = "KEY_MOVIES_ENABLED"
    private const val NEWS_ENABLED = "KEY_NEWS_ENABLED"
    private const val TWITTER_AD_ENABLED = "TWITTER_AD_ENABLED"
    private const val PROGRESS_PERCENT = "KEY_PROGRESS_PERCENT"
    private const val STREAMINGS_ENABLED = "KEY_STREAMINGS_ENABLED"
    private const val THEME = "KEY_THEME"
    private const val USER_ID = "KEY_USER_ID"
    private const val INSTALL_TIMESTAMP = "INSTALL_TIMESTAMP"
    private const val PROGRESS_UPCOMING_COLLAPSED = "PROGRESS_UPCOMING_COLLAPSED"
    private const val PROGRESS_ON_HOLD_COLLAPSED = "PROGRESS_ON_HOLD_COLLAPSED"
    private const val PROGRESS_NEXT_EPISODE_TYPE = "PROGRESS_NEXT_EPISODE_TYPE"
    private const val NEWS_FILTERS = "NEWS_FILTERS"
    private const val NEWS_VIEW_TYPE = "NEWS_VIEW_TYPE"
    private const val LOCALE_INITIALISED = "LOCALE_INITIALISED"
  }

  suspend fun isInitialized() =
    localSource.settings.getCount() > 0

  suspend fun load(): Settings {
    val settingsDb = localSource.settings.getAll()
    return mappers.settings.fromDatabase(settingsDb)
  }

  suspend fun update(settings: Settings) {
    transactions.withTransaction {
      val settingsDb = mappers.settings.toDatabase(settings)
      localSource.settings.upsert(settingsDb)
    }
  }

  var installTimestamp by LongPreference(preferences, INSTALL_TIMESTAMP, 0L)
  var isPremium by BooleanPreference(preferences, PREMIUM)
  var streamingsEnabled by BooleanPreference(preferences, STREAMINGS_ENABLED, true)
  var isMoviesEnabled by BooleanPreference(preferences, MOVIES_ENABLED, true)
  var isNewsEnabled by BooleanPreference(preferences, NEWS_ENABLED)
  var isTwitterAdEnabled by BooleanPreference(preferences, TWITTER_AD_ENABLED, true)
  var language by StringPreference(preferences, LANGUAGE, DEFAULT_LANGUAGE)
  var country by StringPreference(preferences, COUNTRY, DEFAULT_COUNTRY)
  var dateFormat by StringPreference(preferences, DATE_FORMAT, DEFAULT_DATE_FORMAT)

  var isProgressUpcomingCollapsed by BooleanPreference(preferences, PROGRESS_UPCOMING_COLLAPSED)
  var isProgressOnHoldCollapsed by BooleanPreference(preferences, PROGRESS_ON_HOLD_COLLAPSED)
  var progressNextEpisodeType by EnumPreference(preferences, PROGRESS_NEXT_EPISODE_TYPE, LAST_WATCHED, ProgressNextEpisodeType::class.java)
  var newsViewType by StringPreference(preferences, NEWS_VIEW_TYPE, DEFAULT_NEWS_VIEW_TYPE)
  var isLocaleInitialised by BooleanPreference(preferences, LOCALE_INITIALISED, false)

  var mode: Mode
    get() {
      val default = Mode.SHOWS.name
      return Mode.valueOf(preferences.getString(MODE, default) ?: default)
    }
    set(value) = preferences.edit(true) { putString(MODE, value.name) }

  var theme: Int
    get() {
      if (!isPremium) return MODE_NIGHT_YES
      return preferences.getInt(THEME, MODE_NIGHT_YES)
    }
    set(value) = preferences.edit(true) { putInt(THEME, value) }

  var progressPercentType: ProgressType
    get() {
      val setting = preferences.getString(PROGRESS_PERCENT, ProgressType.AIRED.name) ?: ProgressType.AIRED.name
      return ProgressType.valueOf(setting)
    }
    set(value) = preferences.edit(true) { putString(PROGRESS_PERCENT, value.name) }

  val userId
    get() = when (val id = preferences.getString(USER_ID, null)) {
      null -> {
        val uuid = UUID.randomUUID().toString().take(13)
        preferences.edit().putString(USER_ID, uuid).apply()
        uuid
      }
      else -> id
    }

  var newsFilters: List<NewsItem.Type>
    get() {
      val filters = preferences.getString(NEWS_FILTERS, null)
      return when {
        filters.isNullOrBlank() -> emptyList()
        else -> filters.split(",").map { NewsItem.Type.fromSlug(it) }
      }
    }
    set(value) {
      preferences.edit { putString(NEWS_FILTERS, value.joinToString(",") { it.slug }) }
    }

  suspend fun revokePremium() {
    val settings = load()
    update(settings.copy(traktQuickRateEnabled = false))
    isPremium = false
    theme = MODE_NIGHT_YES
    isNewsEnabled = false
    widgets.revokePremium()
  }

  suspend fun clearLanguageLogs() {
    with(localSource) {
      transactions.withTransaction {
        translationsShowsSyncLog.deleteAll()
        translationsMoviesSyncLog.deleteAll()
      }
    }
  }

  suspend fun clearUnusedTranslations(input: List<String>) {
    with(localSource) {
      transactions.withTransaction {
        showTranslations.deleteByLanguage(input)
        movieTranslations.deleteByLanguage(input)
        episodesTranslations.deleteByLanguage(input)
        people.deleteTranslations()
      }
    }
  }
}

package com.michaldrabik.showly2.common.shows

import androidx.room.withTransaction
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_settings.helpers.AppLanguage
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated translations.
 */
@AppScope
class ShowsTranslationsSyncRunner @Inject constructor(
  private val database: AppDatabase,
  private val settingsRepository: SettingsRepository
) {

  suspend fun run(): Int {
    Timber.i("Sync initialized.")
    val language = settingsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) {
      clearUnusedTranslations(language)
      Timber.i("Language is default. Nothing to process. Exiting...")
      return 0
    }

    clearUnusedTranslations(language)
    return 0
  }

  private suspend fun clearUnusedTranslations(language: String) {
    try {
      Timber.i("Deleting unused translations...")
      val toClear = AppLanguage.values()
        .filter { it.code != Config.DEFAULT_LANGUAGE && it.code != language }
        .map { it.code }
      database.withTransaction {
        database.showTranslationsDao().deleteByLanguage(toClear)
        database.episodeTranslationsDao().deleteByLanguage(toClear)
      }
    } catch (error: Throwable) {
      Timber.e(error)
      FirebaseCrashlytics.getInstance().recordException(error)
    }
  }
}

package com.michaldrabik.showly2.common.movies

import androidx.room.withTransaction
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TranslationsSyncProgress
import com.michaldrabik.ui_model.MovieStatus.UNKNOWN
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.movies.MoviesRepository
import com.michaldrabik.ui_settings.helpers.AppLanguage
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated translations.
 */
@AppScope
class MoviesTranslationsSyncRunner @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  companion object {
    private const val DELAY_MS = 10L
  }

  suspend fun run(): Int {
    Timber.i("Sync initialized.")
    val language = settingsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) {
      clearUnusedTranslations(language)
      Timber.i("Language is default. Nothing to process. Exiting...")
      return 0
    }

    if (!settingsRepository.isMoviesEnabled()) {
      Timber.i("Movies are disabled. Exiting...")
      return 0
    }

    val translations = database.movieTranslationsDao().getAll(language)
      .filter { it.overview.isNotBlank() }
    val translationsIds = translations.map { it.idTrakt }

    val moviesToSync = moviesRepository.loadCollection()
      .filter { it.status != UNKNOWN && it.overview.isNotBlank() && it.released != null }
      .filter { it.traktId !in translationsIds }

    if (moviesToSync.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return 0
    }
    Timber.i("Movies to sync: ${moviesToSync.size}.")

    val syncLog = database.translationsMoviesSyncLogDao().getAll()
    var syncCount = 0
    moviesToSync.forEach { movie ->
      try {
        val lastSync = syncLog.find { it.idTrakt == movie.ids.trakt.id }?.syncedAt ?: 0
        if (nowUtcMillis() - lastSync < Config.TRANSLATION_SYNC_COOLDOWN) {
          Timber.i("${movie.title} is on cooldown. No need to sync.")
          return@forEach
        }

        Timber.i("Syncing ${movie.title}(${movie.ids.trakt}) translations...")
        translationsRepository.updateLocalTranslation(movie, language)
        syncCount++
        EventsManager.sendEvent(TranslationsSyncProgress)
        Timber.i("${movie.title}(${movie.ids.trakt}) translation synced.")
      } catch (t: Throwable) {
        Timber.e("${movie.title}(${movie.ids.trakt}) translation sync error. Skipping... \n$t")
      } finally {
        delay(DELAY_MS)
      }
    }

    clearUnusedTranslations(language)
    return syncCount
  }

  private suspend fun clearUnusedTranslations(language: String) {
    try {
      Timber.i("Deleting unused translations...")
      val toClear = AppLanguage.values()
        .filter { it.code != Config.DEFAULT_LANGUAGE && it.code != language }
        .map { it.code }
      database.withTransaction {
        database.movieTranslationsDao().deleteByLanguage(toClear)
      }
    } catch (error: Throwable) {
      Timber.e(error)
      FirebaseCrashlytics.getInstance().recordException(error)
    }
  }
}

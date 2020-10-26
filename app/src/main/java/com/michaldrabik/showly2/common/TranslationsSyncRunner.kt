package com.michaldrabik.showly2.common

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated translations.
 */
@AppScope
class TranslationsSyncRunner @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  companion object {
    private const val DELAY_MS = 250L
  }

  suspend fun run(): Int {
    Timber.i("Sync initialized.")
    val language = settingsRepository.getLanguage()
    if (language === Config.DEFAULT_LANGUAGE) {
      Timber.i("Language is default. Nothing to process. Exiting...")
      return 0
    }

    val translations = database.showTranslationsDao().getAll(language)
    val translationsIds = translations.map { it.idTrakt }

    val showsToSync = showsRepository.myShows.loadAll()
      .plus(showsRepository.seeLaterShows.loadAll())
      .plus(showsRepository.archiveShows.loadAll())
      .distinctBy { it.traktId }
      .filter { it.traktId !in translationsIds }

    if (showsToSync.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return 0
    }
    Timber.i("Shows to sync: ${showsToSync.size}.")

    val syncLog = database.translationsSyncLogDao().getAll()
    var syncCount = 0
    showsToSync.forEach { show ->
      try {
        val lastSync = syncLog.find { it.idTrakt == show.ids.trakt.id }?.syncedAt ?: 0
        if (nowUtcMillis() - lastSync < Config.TRANSLATION_SYNC_COOLDOWN) {
          Timber.i("${show.title} is on cooldown. No need to sync.")
          return@forEach
        }

        Timber.i("Syncing ${show.title}(${show.ids.trakt}) translations...")
        translationsRepository.updateLocalShowTranslation(show, language)
        syncCount++
        Timber.i("${show.title}(${show.ids.trakt}) translation synced.")
      } catch (t: Throwable) {
        Timber.e("${show.title}(${show.ids.trakt}) translation sync error. Skipping... \n$t")
      } finally {
        delay(DELAY_MS)
      }
    }

    return syncCount
  }
}

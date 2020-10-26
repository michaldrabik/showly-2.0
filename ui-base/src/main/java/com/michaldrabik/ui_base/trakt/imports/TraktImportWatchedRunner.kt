package com.michaldrabik.ui_base.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.MyShow
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.mappers.Mappers
import kotlinx.coroutines.delay
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktImportWatchedRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val imagesProvider: ShowImagesProvider,
  private val userTvdbManager: UserTvdbManager,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()

    var syncedCount = 0
    try {
      syncedCount = importWatchedShows(authToken.token)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("importWatchedShows failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        run()
      } else {
        isRunning = false
        retryCount = 0
        throw error
      }
    }

    isRunning = false
    retryCount = 0

    Timber.d("Finished with success.")
    return syncedCount
  }

  private suspend fun importWatchedShows(token: String): Int {
    Timber.d("Importing watched shows...")

    checkTvdbAuth()
    val hiddenShowsIds = cloud.traktApi.fetchHiddenShows(token).map { it.show.ids?.trakt }
    val syncResults = cloud.traktApi.fetchSyncWatched(token, "full")
      .filter { it.show != null }
      .filter { it.show?.ids?.trakt !in hiddenShowsIds }
      .distinctBy { it.show?.ids?.trakt }

    val myShowsIds = database.myShowsDao().getAllTraktIds()
    val seeLaterShowsIds = database.seeLaterShowsDao().getAllTraktIds()
    val archiveShowsIds = database.archiveShowsDao().getAllTraktIds()

    syncResults
      .forEachIndexed { index, result ->
        delay(200)
        Timber.d("Processing \'${result.show!!.title}\'...")
        val showUi = mappers.show.fromNetwork(result.show!!)
        progressListener?.invoke(showUi, index, syncResults.size)

        try {
          val showId = result.show!!.ids!!.trakt!!
          val (seasons, episodes) = loadSeasons(showId, result)

          database.withTransaction {
            if (showId !in myShowsIds && showId !in archiveShowsIds) {
              val show = mappers.show.fromNetwork(result.show!!)
              val showDb = mappers.show.toDatabase(show)

              val timestamp = result.last_watched_at?.let { ZonedDateTime.parse(result.last_watched_at) } ?: nowUtc()
              val myShow = MyShow.fromTraktId(showDb.idTrakt, nowUtcMillis(), timestamp.toMillis())
              database.showsDao().upsert(listOf(showDb))
              database.myShowsDao().insert(listOf(myShow))

              loadImage(show)

              if (showId in seeLaterShowsIds) {
                database.seeLaterShowsDao().deleteById(showId)
              }
            }
            database.seasonsDao().upsert(seasons)
            database.episodesDao().upsert(episodes)
          }

          updateTranslation(showUi)
        } catch (t: Throwable) {
          Timber.w("Processing \'${result.show!!.title}\' failed. Skipping...")
        }
      }

    return syncResults.size
  }

  private suspend fun loadSeasons(showId: Long, item: SyncItem): Pair<List<Season>, List<Episode>> {
    val remoteSeasons = cloud.traktApi.fetchSeasons(showId)
    val localSeasonsIds = database.seasonsDao().getAllWatchedIdsForShows(listOf(showId))
    val localEpisodesIds = database.episodesDao().getAllWatchedIdsForShows(listOf(showId))

    val seasons = remoteSeasons
      .filterNot { localSeasonsIds.contains(it.ids?.trakt) }
      .map { mappers.season.fromNetwork(it) }
      .map { remoteSeason ->
        val isWatched = item.seasons?.any {
          it.number == remoteSeason.number && it.episodes?.size == remoteSeason.episodes.size
        } ?: false
        mappers.season.toDatabase(remoteSeason, IdTrakt(showId), isWatched)
      }

    val episodes = remoteSeasons.flatMap { season ->
      season.episodes
        ?.filterNot { localEpisodesIds.contains(it.ids?.trakt) }
        ?.map { episode ->
          val isWatched = item.seasons
            ?.find { it.number == season.number }?.episodes
            ?.find { it.number == episode.number } != null

          val seasonDb = mappers.season.fromNetwork(season)
          val episodeDb = mappers.episode.fromNetwork(episode)
          mappers.episode.toDatabase(episodeDb, seasonDb, IdTrakt(showId), isWatched)
        } ?: emptyList()
    }

    return Pair(seasons, episodes)
  }

  private suspend fun loadImage(show: Show) {
    try {
      if (!userTvdbManager.isAuthorized()) return
      imagesProvider.loadRemoteImage(show, FANART)
    } catch (t: Throwable) {
      // NOOP Ignore image for now. It will be fetched later if needed.
    }
  }

  private suspend fun checkTvdbAuth() {
    try {
      userTvdbManager.checkAuthorization()
    } catch (t: Throwable) {
      // Ignore for now
    }
  }

  private suspend fun updateTranslation(showUi: Show) {
    try {
      val language = settingsRepository.getLanguage()
      if (language !== Config.DEFAULT_LANGUAGE) {
        Timber.d("Fetching \'${showUi.title}\' translation...")
        translationsRepository.updateLocalShowTranslation(showUi, language)
      }
    } catch (error: Throwable) {
      Timber.w("Processing \'${showUi.title}\' translation failed. Skipping translation...")
    }
  }
}

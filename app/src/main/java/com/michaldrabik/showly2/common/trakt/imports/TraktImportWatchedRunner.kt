package com.michaldrabik.showly2.common.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.common.trakt.TraktSyncRunner
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.UserTvdbManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.MyShow
import com.michaldrabik.storage.database.model.Season
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktImportWatchedRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val imagesProvider: ShowImagesProvider,
  private val userTvdbManager: UserTvdbManager,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()
    val syncedCount = importWatchedShows(authToken.token)

    isRunning = false
    Timber.d("Finished with success.")
    return syncedCount
  }

  private suspend fun importWatchedShows(token: String): Int {
    Timber.d("Importing watched shows...")

    checkTvdbAuth()
    val hiddenShowsIds = cloud.traktApi.fetchHiddenShows(token).map { it.show.ids?.trakt }
    val syncResults = cloud.traktApi.fetchSyncWatched(token, "full")
      .filter { it.show != null && it.show?.ids?.trakt !in hiddenShowsIds }
      .distinctBy { it.show?.ids?.trakt }

    val myShowsIds = database.myShowsDao().getAll().map { it.idTrakt }

    syncResults
      .forEachIndexed { index, result ->
        delay(200)
        Timber.d("Processing \'${result.show!!.title}\'...")
        progressListener?.invoke(result.show!!, index, syncResults.size)

        try {
          val showId = result.show!!.ids!!.trakt!!

          val (seasons, episodes) = loadSeasons(showId, result)

          database.withTransaction {
            if (showId !in myShowsIds) {
              val show = mappers.show.fromNetwork(result.show!!)
              val showDb = mappers.show.toDatabase(show)
              database.showsDao().upsert(listOf(showDb))
              database.myShowsDao().insert(listOf(MyShow.fromTraktId(showDb.idTrakt, nowUtcMillis())))
              loadImage(show)
            }
            database.seasonsDao().upsert(seasons)
            database.episodesDao().upsert(episodes)
          }
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
}

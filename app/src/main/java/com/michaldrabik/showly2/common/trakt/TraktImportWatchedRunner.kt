package com.michaldrabik.showly2.common.trakt

import android.util.Log
import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.MyShow
import com.michaldrabik.storage.database.model.Season
import kotlinx.coroutines.delay
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Show as ShowNetwork

@AppScope
class TraktImportWatchedRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val imagesManager: ImagesManager,
  private val userTraktManager: UserTraktManager
) {

  var progressListener: ((ShowNetwork, Int, Int) -> Unit)? = null
  var isRunning = false

  suspend fun run() {
    isRunning = true
    Log.d(TAG, "Initialized.")
    var authToken = TraktAuthToken()
    try {
      Log.d(TAG, "Checking authorization...")
      authToken = userTraktManager.checkAuthorization()
    } catch (t: Throwable) {
      //TODO Error Oauth needed
    }

    importWatchedShows(authToken)

    isRunning = false
    Log.d(TAG, "Finished with success.")
  }

  private suspend fun importWatchedShows(token: TraktAuthToken) {
    Log.d(TAG, "Importing watched shows...")
    val syncResults = cloud.traktApi.fetchSyncWatched(token.token)
    val myShowsIds = database.myShowsDao().getAll().map { it.idTrakt }

    syncResults.forEachIndexed { index, result ->
      Log.d(TAG, "Processing \'${result.show.title}\'...")
      progressListener?.invoke(result.show, index, syncResults.size)

      try {
        val showId = result.show.ids.trakt

        val (seasons, episodes) = loadSeasons(showId, result)

        database.withTransaction {
          if (showId !in myShowsIds) {
            val show = mappers.show.fromNetwork(result.show)
            val showDb = mappers.show.toDatabase(show)
            database.showsDao().upsert(listOf(showDb))
            database.myShowsDao().insert(listOf(MyShow.fromTraktId(showDb.idTrakt, nowUtcMillis())))
            loadImage(show)
          }
          database.seasonsDao().upsert(seasons)
          database.episodesDao().upsert(episodes)
        }
      } catch (t: Throwable) {
        Log.w(TAG, "Processing \'${result.show.title}\' failed. Skipping...")
      }

      delay(200)
    }
  }

  private suspend fun loadSeasons(showId: Long, item: SyncItem): Pair<List<Season>, List<Episode>> {
    val remoteSeasons = cloud.traktApi.fetchSeasons(showId)

    val seasons = remoteSeasons
      .map { mappers.season.fromNetwork(it) }
      .map { remoteSeason ->
        val isWatched = item.seasons.any {
          it.number == remoteSeason.number && it.episodes.size == remoteSeason.episodes.size
        }
        mappers.season.toDatabase(remoteSeason, IdTrakt(showId), isWatched)
      }

    val episodes = remoteSeasons.flatMap { season ->
      season.episodes.map { episode ->
        val isWatched = item.seasons
          .find { it.number == season.number }?.episodes
          ?.find { it.number == episode.number } != null

        val seasonDb = mappers.season.fromNetwork(season)
        val episodeDb = mappers.episode.fromNetwork(episode)

        mappers.episode.toDatabase(episodeDb, seasonDb, IdTrakt(showId), isWatched)
      }
    }

    return Pair(seasons, episodes)
  }

  private suspend fun loadImage(show: Show) {
    try {
      imagesManager.loadRemoteImage(show, FANART)
    } catch (t: Throwable) {
      //NOOP Ignore image for now. It will be fetched later if needed.
    }
  }

  companion object {
    private const val TAG = "TraktImportWatched"
  }
}
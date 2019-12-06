package com.michaldrabik.showly2.common.trakt

import android.util.Log
import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.MyShow
import javax.inject.Inject

@AppScope
class TraktImportWatchedRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val userTraktManager: UserTraktManager
) {

  suspend fun run() {
    Log.d("TraktImportWatched", "Initialized.")
    var authToken = TraktAuthToken()
    try {
      Log.d("TraktImportWatched", "Checking authorization...")
      authToken = userTraktManager.checkAuthorization()
    } catch (t: Throwable) {
      //TODO Error Oauth needed
    }

    importWatchedShows(authToken)

    Log.d("TraktImportWatched", "Finished with success.")
  }

  private suspend fun importWatchedShows(token: TraktAuthToken) {
    Log.d("TraktImportWatched", "Importing watched shows...")
    val syncResults = cloud.traktApi.fetchSyncWatched(token.token)
    val myShowsIds = database.myShowsDao().getAll().map { it.idTrakt }

    syncResults.forEach { result ->
      Log.d("TraktImportWatched", "Processing \'${result.show.title}\'...")

      val showId = result.show.ids.trakt
      val remoteSeasons = cloud.traktApi.fetchSeasons(showId)

      val seasons = remoteSeasons
        .map { mappers.season.fromNetwork(it) }
        .map { remoteSeason ->
          val isWatched = result.seasons.any {
            it.number == remoteSeason.number && it.episodes.size == remoteSeason.episodes.size
          }
          mappers.season.toDatabase(remoteSeason, IdTrakt(showId), isWatched)
        }

      val episodes = remoteSeasons.flatMap { season ->
        season.episodes.map { episode ->
          val isWatched = result.seasons
            .find { it.number == season.number }?.episodes
            ?.find { it.number == episode.number } != null

          val seasonDb = mappers.season.fromNetwork(season)
          val episodeDb = mappers.episode.fromNetwork(episode)

          mappers.episode.toDatabase(episodeDb, seasonDb, IdTrakt(showId), isWatched)
        }
      }

      database.withTransaction {
        if (myShowsIds.none { it == showId }) {
          val timestamp = nowUtcMillis()
          val showDb = mappers.show.run {
            val show = fromNetwork(result.show)
            toDatabase(show)
          }
          database.showsDao().upsert(listOf(showDb))
          database.myShowsDao().insert(listOf(MyShow.fromTraktId(showDb.idTrakt, timestamp)))
        }
        database.seasonsDao().upsert(seasons)
        database.episodesDao().upsert(episodes)
      }
    }
  }
}
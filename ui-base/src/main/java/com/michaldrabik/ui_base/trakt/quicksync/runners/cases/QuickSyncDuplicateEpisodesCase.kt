package com.michaldrabik.ui_base.trakt.quicksync.runners.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncItem
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class QuickSyncDuplicateEpisodesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource
) {

  suspend fun checkDuplicateEpisodes(
    episodes: List<TraktSyncQueue>,
    fetchedSyncItems: List<SyncItem>
  ): Result {
    if (episodes.isEmpty()) {
      return Result(emptyList(), fetchedSyncItems)
    }
    return withContext(dispatchers.IO) {
      val remoteShows = if (fetchedSyncItems.isNotEmpty()) {
        fetchedSyncItems.toList()
      } else {
        remoteSource.trakt.fetchSyncWatchedShows()
      }
      val duplicateEpisodesIds = mutableListOf<Long>()

      val localEpisodes = localSource.episodes.getAllByShowsIds(
        episodes.filter { it.idList != null }.map { it.idList!! }
      )

      episodes.forEach { item ->
        val showId = item.idList
        showId?.let {
          val localEpisode = localEpisodes.find { it.idTrakt == item.idTrakt }
          if (localEpisode == null) {
            duplicateEpisodesIds.add(item.idTrakt)
          } else {
            if (remoteShows
              .filter { it.show?.ids?.trakt == showId }
              .any { remoteShow ->
                remoteShow.seasons
                  ?.find { it.number == localEpisode.seasonNumber }
                  ?.episodes
                  ?.any { it.number == localEpisode.episodeNumber } == true
              }
            ) {
              duplicateEpisodesIds.add(item.idTrakt)
            }
          }
        }
      }

      Timber.d("Duplicated episodes count: ${duplicateEpisodesIds.size}")

      Result(
        duplicateEpisodesIds = duplicateEpisodesIds,
        remoteShows = remoteShows
      )
    }
  }

  data class Result(
    val duplicateEpisodesIds: List<Long>,
    val remoteShows: List<SyncItem>
  )
}

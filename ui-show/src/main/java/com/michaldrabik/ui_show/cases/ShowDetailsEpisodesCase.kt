package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_base.EpisodesManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.shows.ShowsRepository
import com.michaldrabik.ui_show.helpers.SeasonsBundle
import timber.log.Timber
import javax.inject.Inject

@AppScope
class ShowDetailsEpisodesCase @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(traktId.id) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadSeasons(show: Show, isOnline: Boolean): SeasonsBundle {
    try {
      if (!isOnline) throw Error("App is not online. Should fall back to local data.")

      val remoteSeasons = cloud.traktApi.fetchSeasons(show.ids.trakt.id)
        .map { mappers.season.fromNetwork(it) }

      val isFollowed = showsRepository.myShows.load(show.ids.trakt) != null
      if (isFollowed) {
        episodesManager.invalidateEpisodes(show, remoteSeasons)
      }

      return SeasonsBundle(remoteSeasons, isLocal = false)
    } catch (error: Throwable) {
      // Fall back to local data if remote call fails for any reason
      Timber.d("Falling back to local data. Error: ${error.message}")

      val localEpisodes = database.episodesDao().getAllForShows(listOf(show.traktId))
      val localSeasons = database.seasonsDao().getAllByShowId(show.traktId).map { season ->
        val seasonEpisodes = localEpisodes.filter { ep -> ep.idSeason == season.idTrakt }
        mappers.season.fromDatabase(season, seasonEpisodes)
      }

      return SeasonsBundle(localSeasons, isLocal = true)
    }
  }
}

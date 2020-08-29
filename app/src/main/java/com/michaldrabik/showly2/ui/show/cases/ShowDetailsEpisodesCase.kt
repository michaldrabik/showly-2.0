package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.show.helpers.SeasonsBundle
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesManager
import com.michaldrabik.storage.database.AppDatabase
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

  suspend fun loadSeasons(show: Show): SeasonsBundle {
    try {
      val remoteSeasons = cloud.traktApi.fetchSeasons(show.ids.trakt.id)
        .map { mappers.season.fromNetwork(it) }
      val isFollowed = showsRepository.myShows.load(show.ids.trakt) != null
      if (isFollowed) {
        episodesManager.invalidateEpisodes(show, remoteSeasons)
      }
      return SeasonsBundle(remoteSeasons, isLocal = false)
    } catch (t: Throwable) {
      // Fall back to local data if remote call fails for any reason
      val localEpisodes = database.episodesDao().getAllForShows(listOf(show.traktId))
      val localSeasons = database.seasonsDao().getAllByShowId(show.traktId).map { season ->
        val seasonEpisodes = localEpisodes.filter { ep -> ep.idSeason == season.idTrakt }
        mappers.season.fromDatabase(season, seasonEpisodes)
      }
      return SeasonsBundle(localSeasons, isLocal = true)
    }
  }
}

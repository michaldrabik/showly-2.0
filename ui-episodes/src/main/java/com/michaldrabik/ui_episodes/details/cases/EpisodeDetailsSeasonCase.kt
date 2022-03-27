package com.michaldrabik.ui_episodes.details.cases

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodeDetailsSeasonCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadSeason(showId: IdTrakt, episode: Episode, seasonEpisodes: IntArray?): List<Episode> {
    val episodes = localSource.episodes.getAllByShowId(showId.id, episode.season)
      .map { mappers.episode.fromDatabase(it) }
      .sortedBy { it.number }

    if (episodes.isNotEmpty()) return episodes

    return seasonEpisodes?.map {
      Episode.EMPTY.copy(season = episode.season, number = it)
    } ?: emptyList()
  }
}

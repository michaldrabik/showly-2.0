package com.michaldrabik.ui_episodes.details.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodeDetailsSeasonCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun loadSeason(showId: IdTrakt, episode: Episode) =
    database.episodesDao().getAllByShowId(showId.id, episode.season)
      .map { mappers.episode.fromDatabase(it) }
}

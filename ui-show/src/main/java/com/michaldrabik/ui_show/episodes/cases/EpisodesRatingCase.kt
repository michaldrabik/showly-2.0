package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodesRatingCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(episode: Episode) =
    ratingsRepository.shows.loadRating(episode)

  suspend fun loadRating(season: Season) =
    ratingsRepository.shows.loadRating(season)
}

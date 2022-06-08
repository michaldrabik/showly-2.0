package com.michaldrabik.ui_show.sections.ratings.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRatingCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(episode: Episode) =
    ratingsRepository.shows.loadRating(episode)

  suspend fun loadRating(season: Season) =
    ratingsRepository.shows.loadRating(season)

  suspend fun loadRating(show: Show) =
    ratingsRepository.shows.loadRatings(listOf(show)).firstOrNull()

  suspend fun loadExternalRatings(show: Show) =
    ratingsRepository.shows.external.loadRatings(show)
}

package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesRatingCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(episode: Episode) = withContext(dispatchers.IO) {
    ratingsRepository.shows.loadRating(episode)
  }

  suspend fun loadRating(season: Season) = withContext(dispatchers.IO) {
    ratingsRepository.shows.loadRating(season)
  }
}

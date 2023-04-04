package com.michaldrabik.ui_show.sections.ratings.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRatingCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val ratingsRepository: RatingsRepository,
) {

  suspend fun loadRating(show: Show) = withContext(dispatchers.IO) {
    ratingsRepository.shows.loadRatings(listOf(show)).firstOrNull()
  }

  suspend fun loadExternalRatings(show: Show) = withContext(dispatchers.IO) {
    ratingsRepository.shows.external.loadRatings(show)
  }
}

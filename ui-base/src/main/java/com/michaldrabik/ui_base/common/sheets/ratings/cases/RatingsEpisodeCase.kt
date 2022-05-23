package com.michaldrabik.ui_base.common.sheets.ratings.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.TraktRating
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class RatingsEpisodeCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
) {

  companion object {
    private val RATING_VALID_RANGE = 1..10
  }

  suspend fun loadRating(idTrakt: IdTrakt): TraktRating {
    val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))
    val rating = ratingsRepository.shows.loadRating(episode)
    return rating ?: TraktRating.EMPTY
  }

  suspend fun saveRating(idTrakt: IdTrakt, rating: Int) {
    check(rating in RATING_VALID_RANGE)

    userTraktManager.checkAuthorization()
    val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))

    ratingsRepository.shows.addRating(episode, rating)
  }

  suspend fun deleteRating(idTrakt: IdTrakt) {
    userTraktManager.checkAuthorization()
    val episode = Episode.EMPTY.copy(ids = Ids.EMPTY.copy(trakt = idTrakt))

    ratingsRepository.shows.deleteRating(episode)
  }
}

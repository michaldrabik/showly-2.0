package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ArchiveRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun loadRatings(items: List<ArchiveListItem>): List<ArchiveListItem> {
    if (!userTraktManager.isAuthorized()) return items

    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.preloadShowsRatings(token)

    return items.map {
      val rating = ratingsRepository.shows.loadRating(token, it.show)
      it.copy(userRating = rating?.rating)
    }
  }
}

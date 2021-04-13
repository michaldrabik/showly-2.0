package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import javax.inject.Inject

@AppScope
class MyShowsRatingsCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val userTraktManager: UserTraktManager
) {

  suspend fun loadRatings(items: List<MyShowsItem>): List<MyShowsItem> {
    if (!userTraktManager.isAuthorized()) return items

    val token = userTraktManager.checkAuthorization().token
    ratingsRepository.shows.preloadShowsRatings(token)

    return items.map {
      val rating = ratingsRepository.shows.loadRating(token, it.show)
      it.copy(userRating = rating?.rating)
    }
  }
}

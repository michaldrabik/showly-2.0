package com.michaldrabik.ui_statistics.cases

import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class StatisticsLoadRatingsCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val ratingsRepository: RatingsRepository,
  private val imagesProvider: ShowImagesProvider,
) {

  companion object {
    private const val LIMIT = 25
  }

  suspend fun loadRatings(): List<StatisticsRatingItem> {
    if (!userTraktManager.isAuthorized()) {
      return emptyList()
    }

    val token = userTraktManager.checkAuthorization()
    val ratings = ratingsRepository.shows.loadShowsRatings(token.token)
    val includeArchived = settingsRepository.load().archiveShowsIncludeStatistics

    val ratingsIds = ratings.map { it.idTrakt }
    val myShows = showsRepository.myShows.loadAll(ratingsIds)
    val archivedShows = if (includeArchived) showsRepository.archiveShows.loadAll(ratingsIds) else emptyList()
    val allShows = (myShows + archivedShows).distinctBy { it.traktId }

    return ratings
      .filter { rating -> allShows.any { it.traktId == rating.idTrakt.id } }
      .take(LIMIT)
      .map { rating ->
        val show = allShows.first { it.traktId == rating.idTrakt.id }
        StatisticsRatingItem(
          isLoading = false,
          show = show,
          image = imagesProvider.findCachedImage(show, ImageType.POSTER),
          rating = rating
        )
      }.sortedByDescending { it.rating.ratedAt }
  }
}

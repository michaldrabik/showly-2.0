package com.michaldrabik.showly2.ui.search

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesInteractor
import javax.inject.Inject

@AppScope
class SearchInteractor @Inject constructor(
  private val cloud: Cloud,
  private val imagesInteractor: ImagesInteractor,
  private val mappers: Mappers
) {

  suspend fun searchShows(query: String): List<Show> {
    val shows = cloud.traktApi.fetchShowsSearch(query)
    return shows.map { mappers.show.fromNetwork(it) }
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesInteractor.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesInteractor.loadRemoteImage(show, type, force)
}
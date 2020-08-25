package com.michaldrabik.showly2.ui.search.cases

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Analytics
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SearchMainCase @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository
) {

  suspend fun searchShows(query: String): List<Show> {
    Analytics.logSearchQuery(query)
    val shows = cloud.traktApi.fetchShowsSearch(query)
    return shows.map { mappers.show.fromNetwork(it) }
  }

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun loadSeeLaterShowsIds() = showsRepository.seeLaterShows.loadAllIds()
}

package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsRelatedShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadRelatedShows(show: Show): List<Show> =
    showsRepository.relatedShows.loadAll(show)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
}

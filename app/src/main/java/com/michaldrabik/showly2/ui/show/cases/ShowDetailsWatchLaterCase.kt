package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsWatchLaterCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun isWatchLater(show: Show) =
    showsRepository.seeLaterShows.load(show.ids.trakt) != null

  suspend fun addToWatchLater(show: Show) =
    showsRepository.seeLaterShows.insert(show.ids.trakt)

  suspend fun removeFromWatchLater(show: Show) =
    showsRepository.seeLaterShows.delete(show.ids.trakt)
}

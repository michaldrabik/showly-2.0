package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRelatedShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository
) {

  suspend fun loadRelatedShows(show: Show): List<Show> =
    showsRepository.relatedShows.loadAll(show)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
}

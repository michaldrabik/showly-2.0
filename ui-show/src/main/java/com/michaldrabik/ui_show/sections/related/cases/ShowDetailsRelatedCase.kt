package com.michaldrabik.ui_show.sections.related.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsRelatedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository
) {

  suspend fun loadRelatedShows(show: Show): List<Show> = withContext(dispatchers.IO) {
    val archivedShowsIds = showsRepository.hiddenShows.loadAllIds()
    showsRepository.relatedShows.loadAll(show, archivedShowsIds.size)
      .filter { it.traktId !in archivedShowsIds }
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
  }
}

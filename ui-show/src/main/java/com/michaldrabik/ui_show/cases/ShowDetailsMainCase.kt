package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
) {

  suspend fun loadDetails(idTrakt: IdTrakt) = withContext(dispatchers.IO) {
    showsRepository.detailsShow.load(idTrakt)
  }

  suspend fun removeMalformedShow(idTrakt: IdTrakt) = withContext(dispatchers.IO) {
    with(showsRepository) {
      myShows.delete(idTrakt)
      watchlistShows.delete(idTrakt)
      hiddenShows.delete(idTrakt)
      detailsShow.delete(idTrakt)
    }
    Timber.d("Removing malformed show...")
  }
}

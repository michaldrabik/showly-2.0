package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsMainCase @Inject constructor(
  private val showsRepository: ShowsRepository,
) {

  suspend fun loadDetails(idTrakt: IdTrakt) =
    showsRepository.detailsShow.load(idTrakt)

  suspend fun removeMalformedShow(idTrakt: IdTrakt) {
    with(showsRepository) {
      myShows.delete(idTrakt)
      watchlistShows.delete(idTrakt)
      hiddenShows.delete(idTrakt)
      detailsShow.delete(idTrakt)
    }
    Timber.d("Removing malformed show...")
  }
}

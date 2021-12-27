package com.michaldrabik.ui_base.common.sheets.context_menu.movie.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieContextMenuPinnedCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository,
) {

  fun addToTopPinned(traktId: IdTrakt) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    pinnedItemsRepository.addPinnedItem(movie)
  }

  fun removeFromTopPinned(traktId: IdTrakt) {
    val movie = Movie.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    pinnedItemsRepository.removePinnedItem(movie)
  }
}

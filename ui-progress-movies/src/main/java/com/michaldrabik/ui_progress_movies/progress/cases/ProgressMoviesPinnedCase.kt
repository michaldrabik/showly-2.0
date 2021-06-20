package com.michaldrabik.ui_progress_movies.progress.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressMoviesPinnedCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  fun addPinnedItem(item: Movie) =
    pinnedItemsRepository.addPinnedItem(item)

  fun removePinnedItem(item: Movie) =
    pinnedItemsRepository.removePinnedItem(item)
}

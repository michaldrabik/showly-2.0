package com.michaldrabik.ui_progress_movies.main.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import javax.inject.Inject

class ProgressMoviesPinnedItemsCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  fun addPinnedItem(item: ProgressMovieItem) =
    pinnedItemsRepository.addPinnedItem(item.movie)

  fun removePinnedItem(item: ProgressMovieItem) =
    pinnedItemsRepository.removePinnedItem(item.movie)
}

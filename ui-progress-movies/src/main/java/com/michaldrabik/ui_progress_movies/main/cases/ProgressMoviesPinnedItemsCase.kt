package com.michaldrabik.ui_progress_movies.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_repository.PinnedItemsRepository
import javax.inject.Inject

@AppScope
class ProgressMoviesPinnedItemsCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  fun addPinnedItem(item: ProgressMovieItem) =
    pinnedItemsRepository.addPinnedItem(item.movie)

  fun removePinnedItem(item: ProgressMovieItem) =
    pinnedItemsRepository.removePinnedItem(item.movie)
}

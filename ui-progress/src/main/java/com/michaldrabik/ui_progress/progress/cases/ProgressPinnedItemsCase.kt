package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressPinnedItemsCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository,
) {

  fun addPinnedItem(item: Show) = pinnedItemsRepository.addPinnedItem(item)

  fun removePinnedItem(item: Show) = pinnedItemsRepository.removePinnedItem(item)
}

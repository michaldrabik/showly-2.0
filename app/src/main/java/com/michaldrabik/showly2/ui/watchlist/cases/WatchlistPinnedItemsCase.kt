package com.michaldrabik.showly2.ui.watchlist.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.ui_repository.PinnedItemsRepository
import javax.inject.Inject

@AppScope
class WatchlistPinnedItemsCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  fun addPinnedItem(item: WatchlistItem) {
    val itemId = item.show.traktId
    pinnedItemsRepository.addPinnedItem(itemId)
  }

  fun removePinnedItem(item: WatchlistItem) {
    val itemId = item.show.traktId
    pinnedItemsRepository.removePinnedItem(itemId)
  }
}

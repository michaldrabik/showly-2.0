package com.michaldrabik.ui_watchlist.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_watchlist.WatchlistItem
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

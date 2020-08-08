package com.michaldrabik.showly2.ui.watchlist.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.repository.PinnedItemsRepository
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
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

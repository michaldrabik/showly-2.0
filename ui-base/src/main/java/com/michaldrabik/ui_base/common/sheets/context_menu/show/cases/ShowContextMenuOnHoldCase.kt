package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.repository.OnHoldItemsRepository
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowContextMenuOnHoldCase @Inject constructor(
  private val onHoldItemsRepository: OnHoldItemsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun addToOnHold(traktId: IdTrakt) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    pinnedItemsRepository.removePinnedItem(show)
    onHoldItemsRepository.addItem(show)
    announcementManager.refreshShowsAnnouncements()
  }

  suspend fun removeFromOnHold(traktId: IdTrakt) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    onHoldItemsRepository.removeItem(show)
    announcementManager.refreshShowsAnnouncements()
  }
}

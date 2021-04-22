package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class ShowDetailsListsCase @Inject constructor(
  private val listsRepository: ListsRepository
) {

  suspend fun countLists(show: Show) =
    listsRepository.loadListIdsForItem(IdTrakt(show.traktId), Mode.SHOWS.type).size
}

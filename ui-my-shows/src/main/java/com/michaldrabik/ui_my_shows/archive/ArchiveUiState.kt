package com.michaldrabik.ui_my_shows.archive

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem

data class ArchiveUiState(
  val items: List<ArchiveListItem> = emptyList(),
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null,
)

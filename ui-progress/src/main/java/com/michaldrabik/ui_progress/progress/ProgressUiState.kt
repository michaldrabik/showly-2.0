package com.michaldrabik.ui_progress.progress

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem

data class ProgressUiState(
  val items: List<ProgressListItem>? = null,
  val isLoading: Boolean = false,
  val isOverScrollEnabled: Boolean = false,
  val scrollReset: Event<Boolean>? = null,
  val sortOrder: Event<Triple<SortOrder, SortType, Boolean>>? = null,
)

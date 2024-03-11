package com.michaldrabik.ui_progress.history

import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_progress.history.entities.HistoryListItem

internal data class HistoryUiState(
  val items: List<HistoryListItem> = emptyList(),
  val isLoading: Boolean = false,
  val resetScrollEvent: Event<Boolean>? = null,
)

package com.michaldrabik.ui_progress.progress

import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem

data class ProgressUiState(
  val items: List<ProgressListItem>? = null,
  val isLoading: Boolean = false,
  val scrollReset: ActionEvent<Boolean>? = null,
  val sortOrder: ActionEvent<SortOrder>? = null,
)

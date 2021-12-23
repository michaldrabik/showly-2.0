package com.michaldrabik.ui_base.common.sheets.context_menu.show

import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem

data class ShowContextMenuUiState(
  val isLoading: Boolean? = null,
  val item: ShowContextItem? = null,
)

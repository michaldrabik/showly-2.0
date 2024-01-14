package com.michaldrabik.ui_base.common.sheets.context_menu.events

data class RemoveTraktUiEvent(
  val removeProgress: Boolean = false,
  val removeWatchlist: Boolean = false,
  val removeHidden: Boolean = false
)

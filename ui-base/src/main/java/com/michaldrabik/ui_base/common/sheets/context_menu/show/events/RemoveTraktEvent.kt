package com.michaldrabik.ui_base.common.sheets.context_menu.show.events

data class RemoveTraktEvent(
  val removeProgress: Boolean = false,
  val removeWatchlist: Boolean = false,
  val removeHidden: Boolean = false
)

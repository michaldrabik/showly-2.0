package com.michaldrabik.ui_settings.sections.spoilers.shows

data class SpoilersShowsUiState(
  val isMyShowsHidden: Boolean = false,
  val isWatchlistShowsHidden: Boolean = false,
  val isHiddenShowsHidden: Boolean = false,
  val isNotCollectedShowsHidden: Boolean = false,
)

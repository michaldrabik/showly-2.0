package com.michaldrabik.ui_settings.sections.spoilers.movies

data class SpoilersMoviesUiState(
  val isMyMoviesHidden: Boolean = false,
  val isWatchlistMoviesHidden: Boolean = false,
  val isHiddenMoviesHidden: Boolean = false,
  val isNotCollectedMoviesHidden: Boolean = false,
)

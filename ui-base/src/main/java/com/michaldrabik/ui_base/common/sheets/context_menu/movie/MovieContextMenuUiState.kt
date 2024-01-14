package com.michaldrabik.ui_base.common.sheets.context_menu.movie

import com.michaldrabik.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem

data class MovieContextMenuUiState(
  val isLoading: Boolean? = null,
  val item: MovieContextItem? = null,
)

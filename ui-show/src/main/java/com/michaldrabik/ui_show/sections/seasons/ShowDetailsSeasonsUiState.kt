package com.michaldrabik.ui_show.sections.seasons

import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem

data class ShowDetailsSeasonsUiState(
  val isLoading: Boolean = true,
  val seasons: List<SeasonListItem>? = null,
)

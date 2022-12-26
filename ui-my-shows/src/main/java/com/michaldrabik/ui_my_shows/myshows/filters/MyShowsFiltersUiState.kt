package com.michaldrabik.ui_my_shows.myshows.filters

import com.michaldrabik.ui_model.MyShowsSection

internal data class MyShowsFiltersUiState(
  val sectionType: MyShowsSection? = null,
  val isLoading: Boolean? = null,
)

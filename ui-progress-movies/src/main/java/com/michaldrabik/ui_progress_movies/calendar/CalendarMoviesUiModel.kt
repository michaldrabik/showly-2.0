package com.michaldrabik.ui_progress_movies.calendar

import com.michaldrabik.ui_base.UiModel

data class CalendarMoviesUiModel(
  val isSearching: Boolean? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as CalendarMoviesUiModel).copy(
      isSearching = newModel.isSearching ?: isSearching,
    )
}

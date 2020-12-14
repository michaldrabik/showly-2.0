package com.michaldrabik.ui_progress_movies.calendar

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_progress_movies.ProgressMovieItem

data class ProgressMoviesCalendarUiModel(
  val items: List<ProgressMovieItem>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMoviesCalendarUiModel).copy(
      items = newModel.items?.toList() ?: items
    )
}

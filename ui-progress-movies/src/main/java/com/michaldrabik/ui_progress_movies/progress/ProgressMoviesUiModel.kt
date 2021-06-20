package com.michaldrabik.ui_progress_movies.progress

import com.michaldrabik.ui_base.UiModel

data class ProgressMoviesUiModel(
  val isSearching: Boolean? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMoviesUiModel).copy(
      isSearching = newModel.isSearching ?: isSearching,
    )
}

package com.michaldrabik.ui_progress_movies.main

import com.michaldrabik.ui_base.UiModel

data class ProgressMoviesMainUiModel(
  val searchQuery: String? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMoviesMainUiModel).copy(
      searchQuery = newModel.searchQuery ?: searchQuery
    )
}

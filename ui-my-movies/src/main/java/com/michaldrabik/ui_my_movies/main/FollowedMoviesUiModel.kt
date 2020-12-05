package com.michaldrabik.ui_my_movies.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_my_movies.mymovies.helpers.MyMoviesSearchResult

data class FollowedMoviesUiModel(
  val searchResult: MyMoviesSearchResult? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as FollowedMoviesUiModel)
      .copy(
        searchResult = newModel.searchResult ?: searchResult
      )
}

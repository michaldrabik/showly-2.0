package com.michaldrabik.ui_my_shows.main

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_my_shows.myshows.helpers.MyShowsSearchResult

data class FollowedShowsUiModel(
  val searchResult: MyShowsSearchResult? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as FollowedShowsUiModel)
      .copy(
        searchResult = newModel.searchResult ?: searchResult
      )
}

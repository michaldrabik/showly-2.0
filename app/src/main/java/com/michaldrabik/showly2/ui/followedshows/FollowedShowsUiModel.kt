package com.michaldrabik.showly2.ui.followedshows

import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsSearchResult

data class FollowedShowsUiModel(
  val searchResult: MyShowsSearchResult? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as FollowedShowsUiModel)
      .copy(
        searchResult = newModel.searchResult ?: searchResult
      )
}
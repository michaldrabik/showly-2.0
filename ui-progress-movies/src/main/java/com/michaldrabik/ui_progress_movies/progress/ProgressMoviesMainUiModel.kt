package com.michaldrabik.ui_progress_movies.progress

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress_movies.ProgressMovieItem

data class ProgressMoviesMainUiModel(
  val items: List<ProgressMovieItem>? = null,
  val isSearching: Boolean? = null,
  val resetScroll: ActionEvent<Boolean>? = null,
  val sortOrder: SortOrder? = null,
  val ratingState: RatingState? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as ProgressMoviesMainUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isSearching = newModel.isSearching ?: isSearching,
      resetScroll = newModel.resetScroll ?: resetScroll,
      sortOrder = newModel.sortOrder ?: sortOrder,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}

package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import com.michaldrabik.showly2.model.Comment
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.UiModel
import com.michaldrabik.showly2.ui.show.RatingState

data class EpisodeDetailsUiModel(
  val image: Image? = null,
  val imageLoading: Boolean? = null,
  val comments: List<Comment>? = null,
  val commentsLoading: Boolean? = null,
  val ratingState: RatingState? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as EpisodeDetailsUiModel).copy(
      image = newModel.image ?: image,
      imageLoading = newModel.imageLoading ?: imageLoading,
      comments = newModel.comments?.toList() ?: comments,
      commentsLoading = newModel.commentsLoading ?: commentsLoading,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}

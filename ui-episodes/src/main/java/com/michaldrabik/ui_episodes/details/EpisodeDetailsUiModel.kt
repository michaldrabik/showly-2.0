package com.michaldrabik.ui_episodes.details

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation

data class EpisodeDetailsUiModel(
  val image: Image? = null,
  val imageLoading: Boolean? = null,
  val comments: List<Comment>? = null,
  val commentsLoading: Boolean? = null,
  val ratingState: RatingState? = null,
  val ratingChanged: ActionEvent<Boolean>? = null,
  val translation: ActionEvent<Translation>? = null
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as EpisodeDetailsUiModel).copy(
      image = newModel.image ?: image,
      imageLoading = newModel.imageLoading ?: imageLoading,
      comments = newModel.comments?.toList() ?: comments,
      commentsLoading = newModel.commentsLoading ?: commentsLoading,
      translation = newModel.translation ?: translation,
      ratingChanged = newModel.ratingChanged ?: ratingChanged,
      ratingState = newModel.ratingState?.copy(
        rateLoading = newModel.ratingState.rateLoading ?: ratingState?.rateLoading,
        rateAllowed = newModel.ratingState.rateAllowed ?: ratingState?.rateAllowed,
        userRating = newModel.ratingState.userRating ?: ratingState?.userRating
      ) ?: ratingState
    )
}
